package net.chikina.minecraft.dungeon.combat

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class MagicElement(val displayName: String, val color: TextColor) {
    FIRE("Fire", NamedTextColor.RED),
    WATER("Water", NamedTextColor.BLUE),
    THUNDER("Thunder", NamedTextColor.YELLOW),
    WIND("Wind", NamedTextColor.GREEN),
    EARTH("Earth", NamedTextColor.GOLD)
}
