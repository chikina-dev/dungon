package net.chikina.minecraft.dungeon.map.floating.cave

import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import org.bukkit.Material
import org.bukkit.util.Vector
import java.util.Random

class CavePopulator(
  private val config: FloatingIslandConfig,
) {
  private val random = Random(config.seed)

  data class OreDef(
    val material: Material,
    val deepslateMaterial: Material,
    val size: Int, // Radius of blob
    val attemptsPerChunk: Int, // Frequency
    val minRatio: Double, // 0.0 = Bottom, 1.0 = Top
    val maxRatio: Double,
  )

  // Official Distribution
  private val ores =
    listOf(
      OreDef(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, 3, 20, 0.4, 1.0),
      OreDef(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, 2, 15, 0.2, 0.9),
      OreDef(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, 2, 5, 0.0, 0.4),
      OreDef(Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, 2, 4, 0.1, 0.5),
      OreDef(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, 2, 6, 0.0, 0.3),
      OreDef(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, 1, 5, 0.0, 0.15),
    )

  private val stoneVariants =
    listOf(
      OreDef(Material.GRANITE, Material.GRANITE, 4, 8, 0.3, 1.0),
      OreDef(Material.DIORITE, Material.DIORITE, 4, 8, 0.3, 1.0),
      OreDef(Material.ANDESITE, Material.ANDESITE, 4, 8, 0.3, 1.0),
      OreDef(Material.TUFF, Material.TUFF, 4, 8, 0.0, 0.4),
    )

  fun populate(buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer, graph: CaveGraph) {
    for (node in graph.nodes) {
      if (node == graph.entranceNode) continue

      val isDeepest = (node == graph.deepestNode)

      if (isDeepest) {
        ensureFloor(
          buffer,
          node.center,
          node.radiusX * config.caveScale,
          node.radiusY * config.caveScale,
          node.radiusZ * config.caveScale,
        )
      }

      populateVolume(buffer, node, isDeepest)
    }

    for (edge in graph.edges) {
      populateEdge(buffer, edge)
    }
  }

  private fun populateVolume(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    node: CaveNode,
    isDeepest: Boolean,
  ) {
    val center = node.center
    val rx = (node.radiusX * config.caveScale + 2).toInt()
    val ry = (node.radiusY * config.caveScale + 2).toInt()
    val rz = (node.radiusZ * config.caveScale + 2).toInt()

    val region =
      net.chikina.minecraft.dungeon.util.Region3D(
        (center.x - rx).toInt(),
        (center.y - ry).toInt(),
        (center.z - rz).toInt(),
        (center.x + rx).toInt(),
        (center.y + ry).toInt(),
        (center.z + rz).toInt(),
      )

    val volume = region.width * region.height * region.depth
    if (volume <= 0) return

    val chunks = (volume / 16384.0).coerceAtLeast(0.1)

    for (variant in stoneVariants) {
      val count = (variant.attemptsPerChunk * chunks).toInt()
      for (i in 0 until count) {
        trySpawnVein(buffer, region, variant, false)
      }
    }

    if (isDeepest) {
      val diamond = ores.find { it.material == Material.DIAMOND_ORE }!!
      for (i in 0 until 20) {
        trySpawnVein(buffer, region, diamond, true)
      }
    } else {
      for (ore in ores) {
        val count = (ore.attemptsPerChunk * chunks).toInt()
        for (i in 0 until count) {
          trySpawnVein(buffer, region, ore, false)
        }
      }
    }
  }

  private fun populateEdge(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    edge: CaveEdge,
  ) {
    val mid = edge.from.center
      .clone()
      .add(edge.to.center)
      .multiply(0.5)
    // Small volume around tunnel center
    val region =
      net.chikina.minecraft.dungeon.util.Region3D(
        (mid.x - 5).toInt(),
        (mid.y - 5).toInt(),
        (mid.z - 5).toInt(),
        (mid.x + 5).toInt(),
        (mid.y + 5).toInt(),
        (mid.z + 5).toInt(),
      )

    for (ore in ores) {
      if (random.nextDouble() < 0.3) {
        trySpawnVein(buffer, region, ore, false)
      }
    }
  }

  private fun trySpawnVein(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    region: net.chikina.minecraft.dungeon.util.Region3D,
    def: OreDef,
    force: Boolean,
  ) {
    val x = random.nextInt(region.width) + region.minX
    val y = random.nextInt(region.height) + region.minY
    val z = random.nextInt(region.depth) + region.minZ

    val relativeY = y - config.bounds.minY
    val ratio = relativeY.toDouble() / config.bounds.height.toDouble()

    if (!force) {
      if (ratio < def.minRatio || ratio > def.maxRatio) return
    }

    val currentType = buffer.getBlock(x, y, z)
    if (!isReplaceable(currentType)) return

    val mat =
      if (currentType == Material.DEEPSLATE || currentType == Material.TUFF) {
        def.deepslateMaterial
      } else {
        def.material
      }

    generateBlob(buffer, x, y, z, mat, def.size)
  }

  private fun generateBlob(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    cx: Int,
    cy: Int,
    cz: Int,
    material: Material,
    radius: Int,
  ) {
    val r2 = radius * radius
    val region =
      net.chikina.minecraft.dungeon.util.Region3D(
        cx - radius,
        cy - radius,
        cz - radius,
        cx + radius,
        cy + radius,
        cz + radius,
      )

    region.forEach { x: Int, y: Int, z: Int ->
      val dx = x - cx
      val dy = y - cy
      val dz = z - cz
      if (dx * dx + dy * dy + dz * dz <= r2) {
        val block = buffer.getBlock(x, y, z)
        if (isReplaceable(block)) {
          buffer.setBlock(x, y, z, material)
        }
      }
    }
  }

  private fun isReplaceable(mat: Material): Boolean = mat == Material.STONE ||
    mat == Material.DEEPSLATE ||
    mat == Material.ANDESITE ||
    mat == Material.DIORITE ||
    mat == Material.GRANITE ||
    mat == Material.TUFF

  private fun ensureFloor(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    center: Vector,
    rx: Double,
    ry: Double,
    rz: Double,
  ) {
    val floorY = (center.y - ry).toInt()

    val safeRegion =
      net.chikina.minecraft.dungeon.util.Region3D(
        (center.x - rx).toInt(),
        floorY - 3,
        (center.z - rz).toInt(),
        (center.x + rx).toInt(),
        floorY - 1,
        (center.z + rz).toInt(),
      )

    safeRegion.forEach { x: Int, y: Int, z: Int ->
      val dx = (x - center.x) / rx
      val dz = (z - center.z) / rz
      if (dx * dx + dz * dz <= 1.0) {
        val block = buffer.getBlock(x, y, z)
        if (block == Material.AIR) {
          buffer.setBlock(x, y, z, Material.DEEPSLATE)
        }
      }
    }
  }
}
