package net.chikina.minecraft.dungeon.item

import net.chikina.minecraft.dungeon.util.PluginKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

interface GameItem {
  val itemStack: ItemStack

  var itemId: String?
    get() =
      itemStack.itemMeta?.persistentDataContainer?.get(
        PluginKeys.itemId,
        PersistentDataType.STRING,
      )
    set(value) {
      val meta = itemStack.itemMeta ?: return
      if (value != null) {
        meta.persistentDataContainer.set(
          PluginKeys.itemId,
          PersistentDataType.STRING,
          value,
        )
      }
      itemStack.itemMeta = meta
    }

  fun getPDC(key: NamespacedKey): Int {
    val meta = itemStack.itemMeta ?: return 0
    return meta.persistentDataContainer.get(key, PersistentDataType.INTEGER) ?: 0
  }

  fun setPDC(key: NamespacedKey, value: Int) {
    val meta = itemStack.itemMeta ?: return
    meta.persistentDataContainer.set(key, PersistentDataType.INTEGER, value)
    itemStack.itemMeta = meta
  }

  fun addAttribute(attribute: ItemAttribute) {
    val meta = itemStack.itemMeta ?: return
    val current =
      meta.persistentDataContainer.get(
        PluginKeys.itemAttributes,
        PersistentDataType.STRING,
      )
        ?: ""
    val attributes = current.split(",").filter { it.isNotEmpty() }.toMutableSet()
    attributes.add(attribute.name)
    meta.persistentDataContainer.set(
      PluginKeys.itemAttributes,
      PersistentDataType.STRING,
      attributes.joinToString(","),
    )
    itemStack.itemMeta = meta
  }

  fun hasAttribute(attribute: ItemAttribute): Boolean {
    val meta = itemStack.itemMeta ?: return false
    val current =
      meta.persistentDataContainer.get(
        PluginKeys.itemAttributes,
        PersistentDataType.STRING,
      )
        ?: return false
    return current.split(",").contains(attribute.name)
  }

  fun getAttributes(): Set<String> {
    val meta = itemStack.itemMeta ?: return emptySet()
    val current =
      meta.persistentDataContainer.get(
        PluginKeys.itemAttributes,
        PersistentDataType.STRING,
      )
        ?: return emptySet()
    return current.split(",").filter { it.isNotEmpty() }.toSet()
  }

  companion object {
    operator fun invoke(material: Material): GameItem = DefaultGameItem(ItemStack(material))

    operator fun invoke(itemStack: ItemStack): GameItem = DefaultGameItem(itemStack)

    fun from(itemStack: ItemStack): GameItem = DefaultGameItem(itemStack)

    fun from(material: GameMaterial, amount: Int = 1): GameItem {
      val item = ItemStack(material.material, amount)
      val meta = item.itemMeta
      meta.displayName(Component.text(material.displayName, material.rarity.color))
      meta.lore(material.description.map { Component.text(it, NamedTextColor.GRAY) })

      meta.persistentDataContainer.set(
        PluginKeys.itemId,
        PersistentDataType.STRING,
        material.name,
      )

      item.itemMeta = meta
      return DefaultGameItem(item)
    }
  }

  private class DefaultGameItem(
    override val itemStack: ItemStack,
  ) : GameItem
}
