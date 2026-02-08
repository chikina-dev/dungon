package net.chikina.minecraft.dungeon.tool

import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.util.ItemSkillHelper
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class GrapplingHook : GameItem {
  override val itemStack: ItemStack = ItemStack(Material.FISHING_ROD)

  init {
    val meta = itemStack.itemMeta
    if (meta != null) {
      meta.displayName(Component.text("Grappling Hook", NamedTextColor.GOLD))
      meta.lore(
        listOf(
          Component.text("Right-Click to fire hook", NamedTextColor.GRAY),
          Component.text("Right-Click again to pull", NamedTextColor.GRAY),
        ),
      )
      itemStack.itemMeta = meta
    }
    itemId = "grappling_hook"

    // Set weapon skill
    ItemSkillHelper.setWeaponSkill(itemStack, "grappling_hook")
  }
}
