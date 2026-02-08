package net.chikina.minecraft.dungeon.item

import net.chikina.minecraft.dungeon.util.PluginKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class OreItem(
  val id: String,
  val material: Material,
  val displayName: String,
  val color: NamedTextColor,
  val description: List<String> = emptyList(),
) : GameItem {
  MITHRIL_ORE(
    "mithril_ore",
    Material.PRISMARINE_CRYSTALS,
    "Mithril",
    NamedTextColor.AQUA,
    listOf("A rare mineral."),
  ),
  ;

  override val itemStack: ItemStack by lazy {
    val stack = ItemStack(material)
    val idMeta = stack.itemMeta
    idMeta.persistentDataContainer.set(PluginKeys.itemId, PersistentDataType.STRING, id)
    stack.itemMeta = idMeta

    val lore = mutableListOf<Component>()
    description.forEach { line -> lore.add(Component.text(line, NamedTextColor.GRAY)) }

    val meta = stack.itemMeta
    meta.displayName(Component.text(displayName, color))
    meta.lore(lore)
    stack.itemMeta = meta

    stack
  }
}
