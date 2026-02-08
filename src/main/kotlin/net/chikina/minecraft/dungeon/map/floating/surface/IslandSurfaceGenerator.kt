package net.chikina.minecraft.dungeon.map.floating.surface

import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import net.chikina.minecraft.dungeon.util.Ellipsoid
import net.chikina.minecraft.dungeon.util.Region3D
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector
import java.util.UUID

class IslandSurfaceGenerator(
  private val config: FloatingIslandConfig,
  private val worldUID: UUID,
) {
  // Noises
  private val baseNoise = net.chikina.minecraft.dungeon.math.noise
    .Perlin(config.seed)
  private val detailNoise = net.chikina.minecraft.dungeon.math.noise
    .Perlin(config.seed + 1)
  private val stalactiteNoise = net.chikina.minecraft.dungeon.math.noise
    .Perlin(config.seed + 2)

  companion object {
    private const val WARP_SCALE = 0.02
    private const val WARP_AMP = 20.0
    private const val TREE_CHANCE = 0.05
    private const val DEEPSLATE_CUTOFF = 0.4
    private const val STONE_CUTOFF = 0.5
    private const val SURFACE_NOISE_SCALE = 15.0
    private const val ENTRANCE_ATTEMPTS = 20
    private const val ENTRANCE_RADIUS_RATIO = 0.7
  }

  // Utilities
  private val region =
    Region3D(
      config.bounds.minX,
      config.bounds.minY,
      config.bounds.minZ,
      config.bounds.maxX,
      config.bounds.maxY,
      config.bounds.maxZ,
    )

  private val ellipsoid =
    Ellipsoid(
      region.center,
      config.bounds.width / 2.0,
      config.bounds.height /
        2.0, // Not used strictly directly often due to hemisphere logic
      config.bounds.depth / 2.0,
    )

  private val CENTER_X: Double
    get() = ellipsoid.center.x
  private val CENTER_Z: Double
    get() = ellipsoid.center.z

  // Ellipsoid Radii (Full Bounds for Decoration)
  private val RADIUS_X: Double
    get() = ellipsoid.radiusX
  private val RADIUS_Z: Double
    get() = ellipsoid.radiusZ

  fun generate(buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer) {
    generateBaseShape(buffer)
    generateSurfaceTexturing(buffer)

    val entranceLoc = determineEntranceLocation() // Deterministic
    if (entranceLoc != null) {
      placeEntrance(buffer, entranceLoc)
    }

    generateVegetation(buffer)
  }

  private fun generateBaseShape(buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer) {
    val islandHeight = config.bounds.height.toDouble()
    val minY = config.bounds.minY.toDouble()

    // 1. Pre-calculate Surface Height Map (2D) to avoid re-sampling per Y
    val surfaceHeightMap = DoubleArray(region.width * region.depth)
    for (x in 0 until region.width) {
      val worldX = region.minX + x
      for (z in 0 until region.depth) {
        val worldZ = region.minZ + z

        val surfaceNoise =
          baseNoise.noise(worldX * config.noiseScale, 0.0, worldZ * config.noiseScale) * 0.5 +
            detailNoise.noise(
              worldX * config.noiseScale * 2,
              0.0,
              worldZ * config.noiseScale * 2,
            ) * 0.25
        val surfaceLevel = config.horizonY + (surfaceNoise * SURFACE_NOISE_SCALE)
        surfaceHeightMap[x * region.depth + z] = surfaceLevel
      }
    }

    // 2. Create Interpolators for Warp Noise (3D)
    // cell size 4 is a good balance for warp (low frequency)
    val warpXInterp =
      net.chikina.minecraft.dungeon.math.noise.NoiseInterpolator(
        baseNoise,
        region,
        4,
        WARP_SCALE,
        WARP_SCALE,
        WARP_SCALE,
        WARP_AMP,
      )
    val warpZInterp =
      net.chikina.minecraft.dungeon.math.noise.NoiseInterpolator(
        baseNoise,
        region,
        4,
        WARP_SCALE,
        WARP_SCALE,
        WARP_SCALE,
        WARP_AMP,
        100.0,
        0.0,
        100.0,
      )

    region.forEach { x, y, z ->
      // Optimization: Check Surface Height first (2D check)
      val localX = x - region.minX
      val localZ = z - region.minZ
      // Safety check for bounds (though forEach should match region)
      if (localX in 0 until region.width && localZ in 0 until region.depth) {
        val surfY = surfaceHeightMap[localX * region.depth + localZ]
        if (y > surfY) return@forEach // Cut off above surface
      }

      val density =
        getSolidCoreDensityOptimized(
          x.toDouble(),
          y.toDouble(),
          z.toDouble(),
          warpXInterp,
          warpZInterp,
        )

      if (density > 0) {
        val relativeY = y - minY
        val ratio = relativeY / islandHeight

        var material = Material.STONE
        if (ratio < DEEPSLATE_CUTOFF) {
          material = Material.DEEPSLATE
        } else if (ratio < STONE_CUTOFF) {
          val range = STONE_CUTOFF - DEEPSLATE_CUTOFF
          val deepslateChance = (STONE_CUTOFF - ratio) / range
          if (Math.random() < deepslateChance) {
            material = Material.DEEPSLATE
          }
        }

        // Direct write, no need to check AIR for initial shape generation in void
        buffer.setBlock(x, y, z, material)
      }
    }
  }

  // Optimized version using interpolators and skipping surface check (done outside)
  private fun generateSurfaceTexturing(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
  ) {
    val minX = region.minX
    val maxX = region.maxX
    val minZ = region.minZ
    val maxZ = region.maxZ
    val minY = region.minY
    val maxY = region.maxY

    for (x in minX..maxX) {
      for (z in minZ..maxZ) {
        var y = maxY
        // Scanning down in buffer. Note: buffer.getBlock returns AIR if not set.
        while (y >= minY && buffer.getBlock(x, y, z) == Material.AIR) {
          y--
        }

        if (y < minY) continue

        if (buffer.getBlock(x, y, z) == Material.STONE) {
          if (y > config.horizonY - 5) {
            buffer.setBlock(x, y, z, Material.GRASS_BLOCK)
            for (d in 1..3) {
              if (y - d < minY) break
              if (buffer.getBlock(x, y - d, z) == Material.STONE) {
                buffer.setBlock(x, y - d, z, Material.DIRT)
              }
            }
          }
        }
      }
    }
  }

  private fun generateVegetation(buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer) {
    // Randomly plant trees
    val minX = region.minX
    val maxX = region.maxX
    val minZ = region.minZ
    val maxZ = region.maxZ
    val maxY = region.maxY

    val rnd = java.util.Random(config.seed)

    for (x in minX..maxX) {
      for (z in minZ..maxZ) {
        // Find surface
        var y = maxY
        while (y > config.horizonY && buffer.getBlock(x, y, z) == Material.AIR) {
          y--
        }

        if (buffer.getBlock(x, y, z) == Material.GRASS_BLOCK) {
          if (rnd.nextDouble() < TREE_CHANCE) {
            buildTree(buffer, x, y + 1, z)
          }
        }
      }
    }
  }

  private fun buildTree(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    x: Int,
    y: Int,
    z: Int,
  ) {
    val type = net.chikina.minecraft.dungeon.foraging.TreeType.OAK // Default to Oak for now
    val blueprint = net.chikina.minecraft.dungeon.foraging.TreeStructure
      .createBlueprint(type)

    val logBlocks = HashSet<Long>()
    val leafBlocks = HashSet<Long>()

    // Center the blueprint on the target location
    val startX = x - (blueprint.width / 2)
    val startZ = z - (blueprint.depth / 2)
    val startY = y

    for (ly in 0 until blueprint.height) {
      for (lx in 0 until blueprint.width) {
        for (lz in 0 until blueprint.depth) {
          val material = blueprint.getBlock(lx, ly, lz)
          if (material != Material.AIR) {
            val wx = startX + lx
            val wy = startY + ly
            val wz = startZ + lz

            // Check if space is clear (simple check)
            val current = buffer.getBlock(wx, wy, wz)
            if (current == Material.AIR ||
              current == Material.GRASS_BLOCK ||
              current == Material.DIRT ||
              current.name.endsWith("LEAVES")
            ) {
              buffer.setBlock(wx, wy, wz, material)

              val packed = net.chikina.minecraft.dungeon.util.CoordinatePacker
                .pack(wx, wy, wz)
              if (material == type.log) {
                logBlocks.add(packed)
              } else if (material == type.leaves) {
                leafBlocks.add(packed)
              }
            }
          }
        }
      }
    }

    // Register with TreeManager
    if (logBlocks.isNotEmpty()) {
      val tree =
        net.chikina.minecraft.dungeon.foraging.TreeStructure(
          java.util.UUID.randomUUID(),
          type,
          logBlocks,
        )
      tree.leafBlocks.addAll(leafBlocks)
      tree.worldId = worldUID
      // Center will be calculated lazily by TreeManager

      net.chikina.minecraft.dungeon.foraging.TreeManager
        .registerTree(tree)
    }
  }

  fun determineEntranceLocation(): Location? {
    // Deterministic calculation based on seed
    // Note: This needs to scan the "theoretical" surface.
    // Since we don't have the blocks yet in Parallel mode, we must rely on the density function
    // OR we run Surface first, then Cave.
    // BUT user wants speed.
    // HYBRID: We estimate the surface height using the density function search.

    val rnd = java.util.Random(config.seed)
    var bestLoc: Location? = null

    for (i in 0..ENTRANCE_ATTEMPTS) {
      val rangeX = RADIUS_X * ENTRANCE_RADIUS_RATIO
      val rangeZ = RADIUS_Z * ENTRANCE_RADIUS_RATIO

      val rx = CENTER_X + (rnd.nextDouble() * 2 - 1) * rangeX
      val rz = CENTER_Z + (rnd.nextDouble() * 2 - 1) * rangeZ

      var ry = config.bounds.maxY.toDouble()
      // Raycast down using density function
      while (ry > config.horizonY) {
        if (getSolidCoreDensity(rx, ry, rz) > 0) {
          bestLoc = Location(null, rx, ry + 1, rz)
          return bestLoc
        }
        ry--
      }
    }

    // Fallback
    return Location(null, CENTER_X, config.horizonY.toDouble() + 5, CENTER_Z)
  }

  // Optimized version using interpolators and skipping surface check (done outside)
  private fun getSolidCoreDensityOptimized(
    x: Double,
    y: Double,
    z: Double,
    warpXInterp: net.chikina.minecraft.dungeon.math.noise.NoiseInterpolator,
    warpZInterp: net.chikina.minecraft.dungeon.math.noise.NoiseInterpolator,
  ): Double {
    // Integer coordinates for interpolation lookups
    val ix = x.toInt()
    val iy = y.toInt()
    val iz = z.toInt()

    val warpX = warpXInterp.getNoise(ix, iy, iz)
    val warpZ = warpZInterp.getNoise(ix, iy, iz)

    val dx = x - CENTER_X + warpX
    val dz = z - CENTER_Z + warpZ

    val normX = dx / RADIUS_X
    val normZ = dz / RADIUS_Z

    val ellipsoidalDistSq = (normX * normX) + (normZ * normZ)
    val normalizedDist = Math.sqrt(ellipsoidalDistSq)

    if (normalizedDist > 0.9) return -1.0

    val radialDensity = 1.0 - Math.pow(normalizedDist, 3.0)
    if (radialDensity <= 0) return -1.0

    // Surface calculation moved to outer loop (pre-calculated)

    // Bottom Hemisphere Check
    if (y < config.horizonY) {
      var hemisphereDepth = 0.0
      if (ellipsoidalDistSq < 1.0) {
        hemisphereDepth = Math.sqrt(1.0 - ellipsoidalDistSq)
      }

      val availableHeight = config.horizonY - config.bounds.minY
      val maxDepth = availableHeight * 0.85
      val baseBottomY = config.horizonY - (hemisphereDepth * maxDepth)

      if (y < baseBottomY) return -1.0
    }

    return 1.0
  }

  // Legacy method kept for DetermineEntrance (which doesn't use the full loop optimization yet, or
  // should be updated)
  // Actually, DetermineEntrance only traces down. It can use the legacy expensive check since it's
  // only a few rays.
  private fun getSolidCoreDensity(x: Double, y: Double, z: Double): Double {
    val warpX = baseNoise.noise(x * WARP_SCALE, y * WARP_SCALE, z * WARP_SCALE) * WARP_AMP
    val warpZ =
      baseNoise.noise(x * WARP_SCALE + 100, y * WARP_SCALE, z * WARP_SCALE + 100) * WARP_AMP

    val dx = x - CENTER_X + warpX
    val dz = z - CENTER_Z + warpZ

    val normX = dx / RADIUS_X
    val normZ = dz / RADIUS_Z

    val ellipsoidalDistSq = (normX * normX) + (normZ * normZ)
    val normalizedDist = Math.sqrt(ellipsoidalDistSq)

    if (normalizedDist > 0.9) return -1.0

    val radialDensity = 1.0 - Math.pow(normalizedDist, 3.0)
    if (radialDensity <= 0) return -1.0

    val surfaceNoise =
      baseNoise.noise(x * config.noiseScale, 0.0, z * config.noiseScale) * 0.5 +
        detailNoise.noise(x * config.noiseScale * 2, 0.0, z * config.noiseScale * 2) *
        0.25
    val surfaceLevel = config.horizonY + (surfaceNoise * SURFACE_NOISE_SCALE)

    if (y > surfaceLevel) return -1.0

    if (y < config.horizonY) {
      var hemisphereDepth = 0.0
      if (ellipsoidalDistSq < 1.0) {
        hemisphereDepth = Math.sqrt(1.0 - ellipsoidalDistSq)
      }

      val availableHeight = config.horizonY - config.bounds.minY
      val maxDepth = availableHeight * 0.85
      val baseBottomY = config.horizonY - (hemisphereDepth * maxDepth)

      if (y < baseBottomY) return -1.0
    }

    return 1.0
  }

  private fun placeEntrance(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    entranceLoc: Location,
  ) {
    smoothTerrain(buffer, entranceLoc, 6)
    buildRuinedGateway(buffer, entranceLoc)
  }

  private fun smoothTerrain(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    center: Location,
    radius: Int,
  ) {
    val targetY = center.blockY - 1
    for (x in -radius..radius) {
      for (z in -radius..radius) {
        if (x * x + z * z > radius * radius) continue
        val wx = center.blockX + x
        val wz = center.blockZ + z

        for (y in targetY - 2..targetY) {
          // Ensure foundation
          if (buffer.getBlock(wx, y, wz) == Material.AIR) {
            buffer.setBlock(wx, y, wz, Material.STONE_BRICKS)
          }
        }
        for (y in targetY + 1..targetY + 8) {
          // Clear air
          buffer.setBlock(wx, y, wz, Material.AIR)
        }
      }
    }
  }

  private fun buildRuinedGateway(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    center: Location,
  ) {
    val r = 2
    val h = 4

    for (y in 0..h) {
      buffer.setBlock(
        center.blockX - r,
        center.blockY + y,
        center.blockZ,
        Material.MOSSY_COBBLESTONE,
      )
      buffer.setBlock(
        center.blockX + r,
        center.blockY + y,
        center.blockZ,
        Material.MOSSY_COBBLESTONE,
      )
    }

    for (x in -r..r) {
      buffer.setBlock(center.blockX + x, center.blockY + h, center.blockZ, Material.STONE_BRICKS)
    }

    buffer.setBlock(
      center.blockX - r,
      center.blockY + h + 1,
      center.blockZ,
      Material.STONE_BRICK_WALL,
    )
    buffer.setBlock(
      center.blockX + r,
      center.blockY + h + 1,
      center.blockZ,
      Material.STONE_BRICK_WALL,
    )

    val targetX = CENTER_X
    val targetY = config.horizonY - 25.0
    val targetZ = CENTER_Z

    val start = center.toVector()
    val end = Vector(targetX, targetY, targetZ)

    val cp1 = start.clone().add(Vector(0.0, -10.0, 0.0))
    val mid = start.clone().add(end).multiply(0.5)
    val offset =
      Vector(
        (Math.random() - 0.5) * 20.0,
        (Math.random() - 0.5) * 5.0,
        (Math.random() - 0.5) * 20.0,
      )
    val cp2 = mid.add(offset)

    val steps = 60
    val tunnelRadius = 2.5

    for (i in 0..steps) {
      val t = i.toDouble() / steps
      val point = cubicBezier(start, cp1, cp2, end, t)

      for (bx in -3..3) {
        for (by in -3..3) {
          for (bz in -3..3) {
            val dist = Math.sqrt((bx * bx + by * by + bz * bz).toDouble())
            if (dist <= tunnelRadius) {
              val wx = point.x + bx
              val wy = point.y + by
              val wz = point.z + bz
              if (point.distance(start) > 4.0 || wy < center.y - 2) {
                buffer.setBlock(wx.toInt(), wy.toInt(), wz.toInt(), Material.AIR)
              }
            }
          }
        }
      }
    }
  }

  private fun cubicBezier(p0: Vector, p1: Vector, p2: Vector, p3: Vector, t: Double): Vector {
    val u = 1 - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t

    val x = (p0.x * uuu) + (p1.x * 3 * uu * t) + (p2.x * 3 * u * tt) + (p3.x * ttt)
    val y = (p0.y * uuu) + (p1.y * 3 * uu * t) + (p2.y * 3 * u * tt) + (p3.y * ttt)
    val z = (p0.z * uuu) + (p1.z * 3 * uu * t) + (p2.z * 3 * u * tt) + (p3.z * ttt)

    return Vector(x, y, z)
  }

  fun getCaveStartLocation(): Location = Location(null, CENTER_X, config.horizonY - 25.0, CENTER_Z)
}
