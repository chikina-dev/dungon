package net.chikina.minecraft.dungeon.item

import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.inventory.ItemStack

class MiningItem(override val itemStack: ItemStack) : GameItem {

    var miningSpeed: Int
        get() = getPDC(PluginKeys.MINING_SPEED)
        set(value) = setPDC(PluginKeys.MINING_SPEED, value)

    var breakingPower: Int
        get() = getPDC(PluginKeys.BREAKING_POWER)
        set(value) = setPDC(PluginKeys.BREAKING_POWER, value)

    var miningFortune: Int
        get() = getPDC(PluginKeys.MINING_FORTUNE)
        set(value) = setPDC(PluginKeys.MINING_FORTUNE, value)
}
