package net.chikina.minecraft.dungeon.map

data class ExplorationDungeon(
  val id: Int = 0,
  val name: String,
  val x1: Int,
  val y1: Int,
  val z1: Int,
  val x2: Int,
  val y2: Int,
  val z2: Int,
  val worldName: String,
  val seed: Long,
  val lastUpdated: Long,
)
