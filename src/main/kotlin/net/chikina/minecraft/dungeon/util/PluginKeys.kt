package net.chikina.minecraft.dungeon.util

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

object PluginKeys {
    lateinit var MINING_SPEED: NamespacedKey
        private set
    lateinit var BREAKING_POWER: NamespacedKey
        private set
    lateinit var MINING_FORTUNE: NamespacedKey
        private set
    lateinit var ITEM_ID: NamespacedKey
        private set
    lateinit var GRAPPLING_HOOK: NamespacedKey
        private set
    lateinit var ELEMENT_TYPE: NamespacedKey
        private set
    lateinit var CUSTOM_DAMAGE: NamespacedKey
        private set
    lateinit var SKILL_ID: NamespacedKey
        private set
    lateinit var WEAPON_SKILL: NamespacedKey
        private set

    fun init(plugin: JavaPlugin) {
        MINING_SPEED = NamespacedKey(plugin, "mining_speed")
        BREAKING_POWER = NamespacedKey(plugin, "breaking_power")
        MINING_FORTUNE = NamespacedKey(plugin, "mining_fortune")
        ITEM_ID = NamespacedKey(plugin, "item_id")
        GRAPPLING_HOOK = NamespacedKey(plugin, "grappling_hook")
        ELEMENT_TYPE = NamespacedKey(plugin, "element_type")
        CUSTOM_DAMAGE = NamespacedKey(plugin, "custom_damage")
        SKILL_ID = NamespacedKey(plugin, "skill_id")
        WEAPON_SKILL = NamespacedKey(plugin, "weapon_skill")
    }
}
