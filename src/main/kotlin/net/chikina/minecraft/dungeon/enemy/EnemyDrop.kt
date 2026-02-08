package net.chikina.minecraft.dungeon.enemy

import net.chikina.minecraft.dungeon.item.GameItem

data class EnemyDrop(
  val item: GameItem,
  val chance: Double,
  val minAmount: Int = 1,
  val maxAmount: Int = 1,
)
