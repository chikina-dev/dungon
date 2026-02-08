package net.chikina.minecraft.dungeon.map

data class MapConfig(
  val seed: Long,
  val scaleMultiplier: Double = 1.0,
  val top: Int = 320,
  val minY: Int = 0,
  val baseHeight: Int = 64,
  val seaLevel: Int = 62,
  val maxTerrainY: Int = top,
)
