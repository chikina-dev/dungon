package net.chikina.minecraft.dungeon.mining

interface MiningEntity {
  val miningStats: MiningStats

  fun updateStats()
}
