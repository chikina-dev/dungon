package net.chikina.minecraft.dungeon.item

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack

class WeaponItem(
  override val itemStack: ItemStack,
) : GameItem {
  init {
    if (!hasAttribute(ItemAttribute.WEAPON)) {
      addAttribute(ItemAttribute.WEAPON)
    }
  }

  val attackDamage: Double
    get() {
      if (itemStack.type == Material.AIR) return 0.0
      val meta = itemStack.itemMeta
      if (meta != null && meta.hasAttributeModifiers()) {
        val modifiers = meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE)
        if (modifiers != null && !modifiers.isEmpty()) {
          var damage = 0.0
          for (mod in modifiers) {
            damage += mod.amount
          }
        }
      }

      return when (itemStack.type) {
        Material.WOODEN_SWORD -> 4.0
        Material.GOLDEN_SWORD -> 4.0
        Material.STONE_SWORD -> 5.0
        Material.IRON_SWORD -> 6.0
        Material.DIAMOND_SWORD -> 7.0
        Material.NETHERITE_SWORD -> 8.0
        Material.WOODEN_AXE -> 7.0
        Material.GOLDEN_AXE -> 7.0
        Material.STONE_AXE -> 9.0
        Material.IRON_AXE -> 9.0
        Material.DIAMOND_AXE -> 9.0
        Material.NETHERITE_AXE -> 10.0
        else -> 1.0
      }
    }
}
