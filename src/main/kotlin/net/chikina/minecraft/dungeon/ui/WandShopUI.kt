package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.combat.MagicElement
import net.chikina.minecraft.dungeon.item.WandItem
import net.chikina.minecraft.dungeon.ui.shop.ShopProduct
import net.chikina.minecraft.dungeon.ui.shop.ShopUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class WandShopUI : ShopUI(Component.text("Magic Wand Shop", NamedTextColor.LIGHT_PURPLE), 54) {
  init {
    setupShop()
  }

  private fun setupShop() {
    val borderItem = ItemStack(Material.CYAN_STAINED_GLASS_PANE)
    val meta = borderItem.itemMeta
    meta.displayName(Component.empty())
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
    borderItem.itemMeta = meta
    fillBorder(borderItem)

    val elements = MagicElement.entries

    var col = 3

    for (element in elements) {
      val wandItem = WandItem.create(element)
      val cost = 1000L

      val displayItem = wandItem.clone()
      val dMeta = displayItem.itemMeta
      val lore = dMeta.lore() ?: mutableListOf()
      lore.add(Component.text("Cost: $cost Runes", NamedTextColor.GOLD))
      dMeta.lore(lore)
      displayItem.itemMeta = dMeta

      val product =
        ShopProduct(
          displayItem = displayItem,
          costs = emptyList(),
          resultItem = wandItem,
          runeCost = cost,
        )
      addProduct(2, col, product)
      col++
    }
  }
}
