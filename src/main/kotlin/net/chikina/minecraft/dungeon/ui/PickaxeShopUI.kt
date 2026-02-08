package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.item.OreItem
import net.chikina.minecraft.dungeon.item.PickaxeItem
import net.chikina.minecraft.dungeon.ui.shop.CostItem
import net.chikina.minecraft.dungeon.ui.shop.ShopProduct
import net.chikina.minecraft.dungeon.ui.shop.ShopUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class PickaxeShopUI : ShopUI(Component.text("Pickaxe Shop", NamedTextColor.DARK_BLUE), 54) {
  init {
    setupShop()
  }

  private fun setupShop() {
    val borderItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
    val meta = borderItem.itemMeta
    meta.displayName(Component.empty())
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
    borderItem.itemMeta = meta
    fillBorder(borderItem)

    val plainPickaxes =
      listOf(
        PickaxeItem.WOODEN_PICKAXE,
        PickaxeItem.COPPER_PICKAXE,
        PickaxeItem.IRON_PICKAXE,
        PickaxeItem.GOLD_PICKAXE,
        PickaxeItem.DIAMOND_PICKAXE,
        PickaxeItem.MITHRIL_PICKAXE,
      )

    cursorIndex = 10

    for (p in plainPickaxes) {
      val cost: Long =
        if (p == PickaxeItem.WOODEN_PICKAXE) {
          0
        } else {
          when (p) {
            PickaxeItem.COPPER_PICKAXE -> 100
            PickaxeItem.IRON_PICKAXE -> 500
            PickaxeItem.GOLD_PICKAXE -> 1000
            PickaxeItem.DIAMOND_PICKAXE -> 2000
            PickaxeItem.MITHRIL_PICKAXE -> 5000
            else -> 0
          }
        }

      // Create a clone for display purposes so we don't modify the original item
      val displayItem = p.itemStack.clone()

      // Static cost lore removed; handled by ShopUI dynamically

      val product =
        ShopProduct(
          displayItem = displayItem,
          costs = emptyList(),
          resultItem = p.itemStack, // Original item remains clean
          runeCost = cost,
        )
      addProduct(product)
    }

    val mithrilCost = CostItem(OreItem.MITHRIL_ORE.itemStack, 100)
    val ironCost = CostItem(ItemStack(Material.IRON_INGOT), 100)

    val titaniumStack = PickaxeItem.TITANIUM_PICKAXE.itemStack
    val displayItem = titaniumStack.clone()

    // Static cost lore removed; handled by ShopUI dynamically

    val titaniumProduct =
      ShopProduct(
        displayItem = displayItem,
        costs = listOf(mithrilCost, ironCost),
        resultItem = titaniumStack,
        runeCost = 5000,
      )
    addProduct(2, 8, titaniumProduct)
  }
}
