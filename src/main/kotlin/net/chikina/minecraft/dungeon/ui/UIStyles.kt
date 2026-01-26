package net.chikina.minecraft.dungeon.ui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object UIStyles {
    fun borderGlass(material: Material = Material.BLACK_STAINED_GLASS_PANE): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta.apply { displayName(Component.empty()) }
        }
    }
}
