package net.chikina.minecraft.dungeon.map.floating.config

data class FloatingIslandConfig(
  val seed: Long,
  val bounds: IslandBounds,
  val horizonRatio: Double = 0.8, // 80% surface, 20% cave (relative from bottom up)
  val mainPathRatio: Double = 0.6, // Ratio of nodes that are part of the main path
  val detourRate: Double = 0.3, // Probability/Amount of side paths
  val deadEndCount: Int = 3, // Number of dead ends
  val shortcutStrength: Double = 0.1, // Probability of connecting non-adjacent nodes
  val caveScale: Double = 1.0, // Scale multiplier for the cave system
  val islandRadius: Double = 50.0, // Radius of the island (e.g., for generation shape)
  val noiseScale: Double = 0.05, // Scale for surface/terrain noise
  val stalactiteScale: Double = 0.1, // Scale for underside stalactites
) {
  val horizonY: Int
    get() = bounds.minY + (bounds.height * horizonRatio).toInt()
}

data class IslandBounds(
  val x1: Int,
  val y1: Int,
  val z1: Int,
  val x2: Int,
  val y2: Int,
  val z2: Int,
) {
  val minX = minOf(x1, x2)
  val maxX = maxOf(x1, x2)
  val minY = minOf(y1, y2)
  val maxY = maxOf(y1, y2)
  val minZ = minOf(z1, z2)
  val maxZ = maxOf(z1, z2)

  val width = maxX - minX + 1
  val height = maxY - minY + 1
  val depth = maxZ - minZ + 1
}
