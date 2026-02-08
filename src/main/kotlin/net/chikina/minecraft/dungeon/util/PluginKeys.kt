package net.chikina.minecraft.dungeon.util

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

object PluginKeys {
  lateinit var itemId: NamespacedKey
    private set
  lateinit var elementType: NamespacedKey
    private set
  lateinit var customDamage: NamespacedKey
    private set
  lateinit var skillId: NamespacedKey
    private set
  lateinit var weaponSkill: NamespacedKey
    private set

  fun init(plugin: JavaPlugin) {
    itemId = NamespacedKey(plugin, "item_id")
    elementType = NamespacedKey(plugin, "element_type")
    customDamage = NamespacedKey(plugin, "custom_damage")
    skillId = NamespacedKey(plugin, "skill_id")
    weaponSkill = NamespacedKey(plugin, "weapon_skill")
    itemOwnerKey = NamespacedKey(plugin, "item_owner")
    itemAttributes = NamespacedKey(plugin, "item_attributes")
    itemHp = NamespacedKey(plugin, "item_hp")
    itemDefense = NamespacedKey(plugin, "item_defense")
    itemEndurance = NamespacedKey(plugin, "item_endurance")
  }

  lateinit var itemOwnerKey: NamespacedKey
    private set
  lateinit var itemAttributes: NamespacedKey
    private set
  lateinit var itemHp: NamespacedKey
    private set
  lateinit var itemDefense: NamespacedKey
    private set
  lateinit var itemEndurance: NamespacedKey
    private set
}
