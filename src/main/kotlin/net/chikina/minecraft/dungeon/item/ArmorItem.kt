package net.chikina.minecraft.dungeon.item

import net.chikina.minecraft.dungeon.util.PluginKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ArmorItem(
  override val itemStack: ItemStack,
) : GameItem {
  val hp: Double
    get() = getPDC(PluginKeys.itemHp).toDouble()

  val defense: Double
    get() = getPDC(PluginKeys.itemDefense).toDouble()

  val endurance: Int
    get() = getPDC(PluginKeys.itemEndurance)

  init {
    addAttribute(ItemAttribute.ARMOR)
  }

  companion object {
    private fun create(
      material: Material,
      name: String,
      hp: Int,
      defense: Int,
      endurance: Int,
    ): ArmorItem {
      val item = ItemStack(material)
      val meta = item.itemMeta
      meta.displayName(Component.text(name, NamedTextColor.GREEN))
      meta.isUnbreakable = true

      // Stats Lore
      val lore = mutableListOf<Component>()
      if (hp > 0) lore.add(Component.text("HP +$hp", NamedTextColor.RED))
      if (defense > 0) lore.add(Component.text("防御力 +$defense", NamedTextColor.BLUE))
      if (endurance > 0) {
        lore.add(
          Component.text("強靭 +$endurance", NamedTextColor.YELLOW),
        ) // Endurance implies Poise/Toughness
      }

      meta.lore(lore)

      meta.persistentDataContainer.set(PluginKeys.itemHp, PersistentDataType.INTEGER, hp)
      meta.persistentDataContainer.set(PluginKeys.itemDefense, PersistentDataType.INTEGER, defense)
      meta.persistentDataContainer.set(
        PluginKeys.itemEndurance,
        PersistentDataType.INTEGER,
        endurance,
      )
      meta.persistentDataContainer.set(
        PluginKeys.itemId,
        PersistentDataType.STRING,
        name.lowercase().replace(" ", "_"),
      )

      item.itemMeta = meta
      val armor = ArmorItem(item)
      armor.addAttribute(ItemAttribute.ARMOR)
      return armor
    }

    // Leather Set (Weakest)
    val LEATHER_HELMET = create(Material.LEATHER_HELMET, "Leather Helmet", 10, 2, 0)
    val LEATHER_CHESTPLATE = create(Material.LEATHER_CHESTPLATE, "Leather Chestplate", 20, 5, 2)
    val LEATHER_LEGGINGS = create(Material.LEATHER_LEGGINGS, "Leather Leggings", 15, 3, 1)
    val LEATHER_BOOTS = create(Material.LEATHER_BOOTS, "Leather Boots", 10, 2, 0)

    // Iron Set (Mid)
    val IRON_HELMET = create(Material.IRON_HELMET, "Iron Helmet", 30, 10, 5)
    val IRON_CHESTPLATE = create(Material.IRON_CHESTPLATE, "Iron Chestplate", 50, 20, 10)
    val IRON_LEGGINGS = create(Material.IRON_LEGGINGS, "Iron Leggings", 40, 15, 8)
    val IRON_BOOTS = create(Material.IRON_BOOTS, "Iron Boots", 30, 10, 5)

    // Golden Set (High Poise/Endurance focus?)
    val GOLDEN_HELMET = create(Material.GOLDEN_HELMET, "Golden Helmet", 20, 5, 10)
    val GOLDEN_CHESTPLATE = create(Material.GOLDEN_CHESTPLATE, "Golden Chestplate", 40, 10, 20)
    val GOLDEN_LEGGINGS = create(Material.GOLDEN_LEGGINGS, "Golden Leggings", 30, 8, 15)
    val GOLDEN_BOOTS = create(Material.GOLDEN_BOOTS, "Golden Boots", 20, 5, 10)

    // Diamond Set (Strong)
    val DIAMOND_HELMET = create(Material.DIAMOND_HELMET, "Diamond Helmet", 100, 30, 15)
    val DIAMOND_CHESTPLATE = create(Material.DIAMOND_CHESTPLATE, "Diamond Chestplate", 200, 50, 30)
    val DIAMOND_LEGGINGS = create(Material.DIAMOND_LEGGINGS, "Diamond Leggings", 150, 40, 20)
    val DIAMOND_BOOTS = create(Material.DIAMOND_BOOTS, "Diamond Boots", 100, 30, 15)

    // Netherite Set (End Game)
    val NETHERITE_HELMET = create(Material.NETHERITE_HELMET, "Netherite Helmet", 200, 50, 25)
    val NETHERITE_CHESTPLATE =
      create(Material.NETHERITE_CHESTPLATE, "Netherite Chestplate", 400, 80, 50)
    val NETHERITE_LEGGINGS = create(Material.NETHERITE_LEGGINGS, "Netherite Leggings", 300, 60, 35)
    val NETHERITE_BOOTS = create(Material.NETHERITE_BOOTS, "Netherite Boots", 200, 50, 25)
  }
}
