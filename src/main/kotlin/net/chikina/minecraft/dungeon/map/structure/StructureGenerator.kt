package net.chikina.minecraft.dungeon.map.structure

import net.chikina.minecraft.dungeon.foraging.TreeManager
import net.chikina.minecraft.dungeon.foraging.TreeStructure
import net.chikina.minecraft.dungeon.map.Chunk
import net.chikina.minecraft.dungeon.map.MapConstants
import net.chikina.minecraft.dungeon.map.MapGenerator
import net.chikina.minecraft.dungeon.util.CoordinatePacker
import org.bukkit.Material
import java.util.Random
import java.util.UUID

class StructureGenerator(
  private val seed: Long,
) {
  private val structures = mutableListOf<DungeonStructure>()
  private val random = Random(seed)

  fun register(structure: DungeonStructure) {
    structures.add(structure)
    structures.sortBy { it.rules.zIndex }
  }

  fun populate(chunk: Chunk, mapGenerator: MapGenerator) {
    val candidates = mutableListOf<StructureCandidate>()
    val neighborRange = 1

    for (cx in chunk.x - neighborRange..chunk.x + neighborRange) {
      for (cz in chunk.z - neighborRange..chunk.z + neighborRange) {
        val chunkSeed = seed + (cx * 341873128712L) + (cz * 132897987541L)
        val chunkRandom = Random(chunkSeed)

        for (structure in structures) {
          val attempts = 10
          for (i in 0 until attempts) {
            if (chunkRandom.nextDouble() > structure.rules.spawnChance) continue

            val absX = cx * MapConstants.CHUNK_WIDTH + chunkRandom.nextInt(MapConstants.CHUNK_WIDTH)
            val absZ =
              cz * MapConstants.CHUNK_HEIGHT + chunkRandom.nextInt(MapConstants.CHUNK_HEIGHT)

            val footprint = structure.getFootprint()
            var minSurfaceY = Int.MAX_VALUE

            if (footprint.isEmpty()) {
              minSurfaceY =
                mapGenerator.getTerrainHeight(
                  absX + structure.width / 2,
                  absZ + structure.depth / 2,
                ) + 1
            } else {
              for ((fx, fz) in footprint) {
                val ty = mapGenerator.getTerrainHeight(absX + fx, absZ + fz) + 1
                if (ty < minSurfaceY) {
                  minSurfaceY = ty
                }
              }
            }

            val env = mapGenerator.getEnvironment(absX, absZ)

            if (structure.rules.conditions.all {
                it.isValid(env, minSurfaceY, mapGenerator.config.seaLevel)
              }
            ) {
              candidates.add(StructureCandidate(structure, absX, absZ, minSurfaceY))
            }
          }
        }
      }
    }

    val accepted = mutableListOf<StructureCandidate>()
    for (candidate in candidates) {
      var conflicting = false

      for (existing in accepted) {
        val requiredSpacing =
          kotlin.math.max(
            candidate.structure.rules.minSpacing,
            existing.structure.rules.minSpacing,
          )

        val r1MinX = candidate.x - requiredSpacing
        val r1maxX = candidate.x + candidate.structure.width + requiredSpacing
        val r1MinZ = candidate.z - requiredSpacing
        val r1MaxZ = candidate.z + candidate.structure.depth + requiredSpacing

        val r2MinX = existing.x
        val r2MaxX = existing.x + existing.structure.width
        val r2MinZ = existing.z
        val r2MaxZ = existing.z + existing.structure.depth

        if (r1MinX < r2MaxX && r1maxX > r2MinX && r1MinZ < r2MaxZ && r1MaxZ > r2MinZ) {
          conflicting = true
          break
        }
      }

      if (!conflicting) {
        accepted.add(candidate)
      }
    }

    // 3. Place accepted structures
    for (cand in accepted) {
      placeStructure(chunk, cand.structure, cand.x, cand.z, cand.y)
    }
  }

  private data class StructureCandidate(
    val structure: DungeonStructure,
    val x: Int,
    val z: Int,
    val y: Int,
  )

  private fun placeStructure(
    chunk: Chunk,
    structure: DungeonStructure,
    worldOriginX: Int,
    worldOriginZ: Int,
    surfaceY: Int,
  ) {
    val chunkStartX = chunk.x * MapConstants.CHUNK_WIDTH
    val chunkStartZ = chunk.z * MapConstants.CHUNK_HEIGHT

    val isTree = structure is TreeStructure.Blueprint
    val treeId =
      if (isTree) {
        UUID.nameUUIDFromBytes("Tree:$worldOriginX:$worldOriginZ".toByteArray())
      } else {
        null
      }

    if (isTree && treeId != null) {
      val blueprint = structure as TreeStructure.Blueprint
      // Initialize tree if not exists
      TreeManager.getOrCreateTree(treeId) { TreeStructure(treeId, blueprint.type) }
    }

    for (y in 0 until structure.height) {
      val worldY = surfaceY + y // Start from surface up
      if (worldY >= chunk.top) continue

      for (z in 0 until structure.depth) {
        val absZ = worldOriginZ + z
        val localZ = absZ - chunkStartZ

        // This is key: Only draw if this block falls into the CURRENT chunk
        if (localZ !in 0 until MapConstants.CHUNK_HEIGHT) continue

        for (x in 0 until structure.width) {
          val absX = worldOriginX + x
          val localX = absX - chunkStartX

          if (localX !in 0 until MapConstants.CHUNK_WIDTH) continue

          val material = structure.getBlock(x, y, z)
          if (material != Material.AIR) {
            // We can check if we are overwriting something we shouldn't?
            // But usually structures win.
            chunk.setBlock(localX, localZ, worldY, material.ordinal)

            if (isTree && treeId != null) {
              if (material == structure.type.log) {
                val packed = CoordinatePacker.pack(absX, worldY, absZ)
                TreeManager.addLogBlock(treeId, packed)
              } else if (material == structure.type.leaves) {
                val packed = CoordinatePacker.pack(absX, worldY, absZ)
                TreeManager.addLeafBlock(treeId, packed)
              }
            }
          }
        }
      }
    }
  }

  private fun getSurfaceHeight(chunk: Chunk, localX: Int, localZ: Int): Int {
    for (y in chunk.top - 1 downTo chunk.minY) {
      if (chunk.getBlock(localX, localZ, y) != 0) { // 0 is AIR usually, or default
        // Check if it's not air if ordinal 0 is air. Default int array is 0.
        // We need to know what ordinal 0 is. Material.AIR.ordinal is usually 0 but better safe.
        if (chunk.getBlock(localX, localZ, y) != Material.AIR.ordinal) {
          return y + 1
        }
      }
    }
    return chunk.minY + 64 // fallback
  }

  private fun isValidGround(chunk: Chunk, localX: Int, localZ: Int, y: Int): Boolean {
    val blockId = chunk.getBlock(localX, localZ, y)
    if (blockId == 0) return false // Air is not ground

    val material = Material.values().getOrNull(blockId) ?: return false
    return when (material) {
      Material.GRASS_BLOCK,
      Material.DIRT,
      Material.STONE,
      Material.GRAVEL,
      Material.SAND,
      Material.RED_SAND,
      Material.SANDSTONE,
      Material.TERRACOTTA,
      Material.PODZOL,
      Material.MYCELIUM,
      Material.SNOW_BLOCK,
      Material.PACKED_ICE,
      Material.ICE,
      -> true

      else -> false
    }
  }
}
