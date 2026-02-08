package net.chikina.minecraft.dungeon.item

import net.chikina.minecraft.dungeon.combat.MagicElement
import net.chikina.minecraft.dungeon.util.PluginKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class WandItem(
  override val itemStack: ItemStack,
) : GameItem {
  val element: MagicElement?
    get() {
      val meta = itemStack.itemMeta ?: return null
      val typeStr =
        meta.persistentDataContainer.get(PluginKeys.elementType, PersistentDataType.STRING)
      return if (typeStr != null) {
        try {
          MagicElement.valueOf(typeStr)
        } catch (e: Exception) {
          null
        }
      } else {
        null
      }
    }

  val attackDamage: Double = 2.0

  companion object {
    fun create(element: MagicElement): ItemStack {
      val item = ItemStack(Material.STICK)
      val meta = item.itemMeta
      meta.displayName(Component.text("${element.displayName} Wand", element.color))
      meta.persistentDataContainer.set(
        PluginKeys.elementType,
        PersistentDataType.STRING,
        element.name,
      )
      meta.persistentDataContainer.set(
        PluginKeys.itemId,
        PersistentDataType.STRING,
        "wand_${element.name.lowercase()}",
      )

      val lore =
        listOf(
          Component.text(
            "Magic Wand imbued with ${element.displayName}",
            NamedTextColor.GRAY,
          ),
          Component.text(
            "Faith increases effect chance and potency",
            NamedTextColor.DARK_GRAY,
          ),
        )
      meta.lore(lore)

      item.itemMeta = meta
      val gameItem = GameItem(item)
      gameItem.addAttribute(ItemAttribute.WEAPON)
      gameItem.addAttribute(ItemAttribute.WAND)

      return gameItem.itemStack
    }
  }
}
