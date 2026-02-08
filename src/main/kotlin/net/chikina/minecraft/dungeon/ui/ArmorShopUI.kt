package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.item.ArmorItem
import net.chikina.minecraft.dungeon.ui.shop.ShopProduct
import net.chikina.minecraft.dungeon.ui.shop.ShopUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ArmorShopUI : ShopUI(Component.text("Armor Shop", NamedTextColor.BLUE), 54) {
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

    // Layout: Rows for each material type
    // Row 1 (y=2): Leather
    // Row 2 (y=3): Iron
    // Row 3 (y=4): Golden
    // Row 4 (y=5): Diamond
    // Netherite likely special/expensive

    val leatherSet =
      listOf(
        ArmorItem.LEATHER_HELMET,
        ArmorItem.LEATHER_CHESTPLATE,
        ArmorItem.LEATHER_LEGGINGS,
        ArmorItem.LEATHER_BOOTS,
      )
    addArmorRow(2, leatherSet, 100)

    val ironSet =
      listOf(
        ArmorItem.IRON_HELMET,
        ArmorItem.IRON_CHESTPLATE,
        ArmorItem.IRON_LEGGINGS,
        ArmorItem.IRON_BOOTS,
      )
    addArmorRow(3, ironSet, 500)

    val goldenSet =
      listOf(
        ArmorItem.GOLDEN_HELMET,
        ArmorItem.GOLDEN_CHESTPLATE,
        ArmorItem.GOLDEN_LEGGINGS,
        ArmorItem.GOLDEN_BOOTS,
      )
    addArmorRow(4, goldenSet, 1000)

    val diamondSet =
      listOf(
        ArmorItem.DIAMOND_HELMET,
        ArmorItem.DIAMOND_CHESTPLATE,
        ArmorItem.DIAMOND_LEGGINGS,
        ArmorItem.DIAMOND_BOOTS,
      )
    addArmorRow(5, diamondSet, 5000)

    // Netherite (Separate?)
  }

  private fun addArmorRow(row: Int, set: List<ArmorItem>, baseCost: Long) {
    // Set order: Helmet, Chest, Leggings, Boots
    // Columns: 3, 4, 5, 6 (Center aligned)

    var col = 3
    for (item in set) {
      val costMultiplier =
        when (col) {
          3 -> 0.8

          // Helmet
          4 -> 1.25

          // Chest
          5 -> 1.0

          // Leggings
          6 -> 0.6

          // Boots
          else -> 1.0
        }
      val cost = (baseCost * costMultiplier).toLong()

      val displayItem = item.itemStack.clone()
      val product =
        ShopProduct(
          displayItem = displayItem,
          costs = emptyList(), // Can add material costs here if needed
          resultItem = item.itemStack,
          runeCost = cost,
        )
      addProduct(row, col, product)
      col++
    }
  }
}
