package net.chikina.minecraft.dungeon.map

data class Environment(
  val x: Double,
  val z: Double,
  val elevation: Double,
  val temperature: Double,
  val humidity: Double,
  val resources: Double,
  val magic: Double,
)
