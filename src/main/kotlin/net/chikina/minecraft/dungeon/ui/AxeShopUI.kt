package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.item.AxeItem
import net.chikina.minecraft.dungeon.item.OreItem
import net.chikina.minecraft.dungeon.ui.shop.CostItem
import net.chikina.minecraft.dungeon.ui.shop.ShopProduct
import net.chikina.minecraft.dungeon.ui.shop.ShopUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class AxeShopUI : ShopUI(Component.text("Axe Shop", NamedTextColor.DARK_GREEN), 54) {
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

    val plainAxes =
      listOf(
        AxeItem.WOODEN_AXE,
        AxeItem.COPPER_AXE,
        AxeItem.IRON_AXE,
        AxeItem.GOLD_AXE,
        AxeItem.DIAMOND_AXE,
        AxeItem.MITHRIL_AXE,
      )

    cursorIndex = 10

    for (item in plainAxes) {
      val cost: Long =
        if (item == AxeItem.WOODEN_AXE) {
          0
        } else {
          when (item) {
            AxeItem.COPPER_AXE -> 100
            AxeItem.IRON_AXE -> 500
            AxeItem.GOLD_AXE -> 1000
            AxeItem.DIAMOND_AXE -> 2000
            AxeItem.MITHRIL_AXE -> 5000
            else -> 0
          }
        }

      // Create a clone for display purposes so we don't modify the original item
      val displayItem = item.itemStack.clone()

      // Static cost lore removed; handled by ShopUI dynamically

      val product =
        ShopProduct(
          displayItem = displayItem,
          costs = emptyList(),
          resultItem = item.itemStack, // Original item remains clean
          runeCost = cost,
        )
      addProduct(product)
    }

    val mithrilCost = CostItem(OreItem.MITHRIL_ORE.itemStack, 100)
    val ironCost = CostItem(ItemStack(Material.IRON_INGOT), 100)

    val titaniumStack = AxeItem.TITANIUM_AXE.itemStack
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
