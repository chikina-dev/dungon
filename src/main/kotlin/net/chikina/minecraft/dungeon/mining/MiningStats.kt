package net.chikina.minecraft.dungeon.mining

/** 採掘に関連するステータス。 */
data class MiningStats(
  var speed: Int = 100, // 採掘速度
  var breakingPower: Int = 1, // 破壊力
  var fortune: Int = 100, // 採掘運 (割合)
)
