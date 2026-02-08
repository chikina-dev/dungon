package net.chikina.minecraft.dungeon.ui.shop

import org.bukkit.inventory.ItemStack

data class CostItem(
  val material: ItemStack,
  val amount: Int,
)

data class ShopProduct(
  val displayItem: ItemStack,
  val costs: List<CostItem>,
  val resultItem: ItemStack,
  val runeCost: Long = 0,
)
