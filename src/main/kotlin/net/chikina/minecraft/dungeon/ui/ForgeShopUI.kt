package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.ui.shop.CostItem
import net.chikina.minecraft.dungeon.ui.shop.ShopProduct
import net.chikina.minecraft.dungeon.ui.shop.ShopUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ForgeShopUI : ShopUI(Component.text("Forge Shop", NamedTextColor.DARK_RED), 54) {
  init {
    setupShop()
  }

  private fun setupShop() {
    val borderItem = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    val meta = borderItem.itemMeta
    meta.displayName(Component.empty())
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
    borderItem.itemMeta = meta
    fillBorder(borderItem)

    val ironProducts =
      listOf(
        createSmeltProduct(1, Material.RAW_IRON, Material.IRON_INGOT, "Iron"),
        createSmeltProduct(10, Material.RAW_IRON, Material.IRON_INGOT, "Iron"),
        createSmeltProduct(100, Material.RAW_IRON, Material.IRON_INGOT, "Iron"),
      )
    addProducts(2, 4, ironProducts)

    val copperProducts =
      listOf(
        createSmeltProduct(1, Material.RAW_COPPER, Material.COPPER_INGOT, "Copper"),
        createSmeltProduct(
          10,
          Material.RAW_COPPER,
          Material.COPPER_INGOT,
          "Copper",
        ),
        createSmeltProduct(
          100,
          Material.RAW_COPPER,
          Material.COPPER_INGOT,
          "Copper",
        ),
      )
    addProducts(3, 4, copperProducts)

    val goldProducts =
      listOf(
        createSmeltProduct(1, Material.RAW_GOLD, Material.GOLD_INGOT, "Gold"),
        createSmeltProduct(10, Material.RAW_GOLD, Material.GOLD_INGOT, "Gold"),
        createSmeltProduct(100, Material.RAW_GOLD, Material.GOLD_INGOT, "Gold"),
      )
    addProducts(4, 4, goldProducts)
  }

  private fun createSmeltProduct(
    amount: Int,
    input: Material,
    output: Material,
    name: String,
  ): ShopProduct {
    val displayItem = ItemStack(output)
    val meta = displayItem.itemMeta
    meta.displayName(Component.text("Smelt x$amount $name", NamedTextColor.GOLD))

    val lore = mutableListOf<Component>()
    lore.add(Component.text("Cost:", NamedTextColor.GRAY))
    val inputName =
      input.name.lowercase().replace("_", " ").split(" ").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
      }
    lore.add(Component.text("- $amount $inputName", NamedTextColor.WHITE))
    lore.add(Component.text("- $amount Coal", NamedTextColor.WHITE))
    meta.lore(lore)

    displayItem.itemMeta = meta
    displayItem.amount = if (amount > 64) 64 else amount

    val costs =
      listOf(
        CostItem(ItemStack(input), amount),
        CostItem(ItemStack(Material.COAL), amount),
      )

    val result = ItemStack(output, amount)

    return ShopProduct(displayItem, costs, result)
  }
}
