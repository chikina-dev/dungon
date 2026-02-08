package net.chikina.minecraft.dungeon.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ItemSkillHelper {
  fun getWeaponSkill(item: ItemStack?): String? {
    if (item == null || item.type == Material.AIR) return null
    val meta = item.itemMeta ?: return null
    return meta.persistentDataContainer.get(PluginKeys.weaponSkill, PersistentDataType.STRING)
  }

  fun setWeaponSkill(item: ItemStack, skillId: String) {
    val meta = item.itemMeta ?: return
    meta.persistentDataContainer.set(PluginKeys.weaponSkill, PersistentDataType.STRING, skillId)
    item.itemMeta = meta
  }
}
