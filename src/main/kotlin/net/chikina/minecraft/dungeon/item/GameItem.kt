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
                        PluginKeys.ITEM_ID,
                        PersistentDataType.STRING
                )
        set(value) {
            val meta = itemStack.itemMeta ?: return
            if (value != null) {
                meta.persistentDataContainer.set(
                        PluginKeys.ITEM_ID,
                        PersistentDataType.STRING,
                        value
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

    companion object {
        operator fun invoke(material: Material): GameItem {
            return DefaultGameItem(ItemStack(material))
        }

        operator fun invoke(itemStack: ItemStack): GameItem {
            return DefaultGameItem(itemStack)
        }

        fun from(itemStack: ItemStack): GameItem {
            return DefaultGameItem(itemStack)
        }

        fun from(material: GameMaterial, amount: Int = 1): GameItem {
            val item = ItemStack(material.material, amount)
            val meta = item.itemMeta
            meta.displayName(Component.text(material.displayName, material.rarity.color))
            meta.lore(material.description.map { Component.text(it, NamedTextColor.GRAY) })

            meta.persistentDataContainer.set(
                    PluginKeys.ITEM_ID,
                    PersistentDataType.STRING,
                    material.name
            )

            item.itemMeta = meta
            return DefaultGameItem(item)
        }
    }

    private class DefaultGameItem(override val itemStack: ItemStack) : GameItem
}
