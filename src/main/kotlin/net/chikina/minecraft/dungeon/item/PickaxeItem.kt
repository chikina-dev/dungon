package net.chikina.minecraft.dungeon.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class PickaxeItem(
  val id: String,
  val material: Material,
  val displayName: String,
  val color: NamedTextColor,
  val speed: Int,
  val power: Int,
  val fortune: Int,
  val description: List<String> = emptyList(),
) : GameItem {
  WOODEN_PICKAXE(
    "wooden_pickaxe",
    Material.WOODEN_PICKAXE,
    "木のツルハシ",
    NamedTextColor.GREEN,
    100,
    1,
    0,
  ),
  COPPER_PICKAXE(
    "copper_pickaxe",
    Material.STONE_PICKAXE,
    "銅のツルハシ",
    NamedTextColor.GREEN,
    150,
    2,
    0,
  ),
  IRON_PICKAXE("iron_pickaxe", Material.IRON_PICKAXE, "鉄のツルハシ", NamedTextColor.GREEN, 200, 3, 0),
  GOLD_PICKAXE("gold_pickaxe", Material.GOLDEN_PICKAXE, "金のツルハシ", NamedTextColor.GREEN, 250, 3, 0),
  DIAMOND_PICKAXE(
    "diamond_pickaxe",
    Material.DIAMOND_PICKAXE,
    "ダイヤモンドのツルハシ",
    NamedTextColor.GREEN,
    300,
    4,
    0,
  ),
  MITHRIL_PICKAXE(
    "mithril_pickaxe",
    Material.DIAMOND_PICKAXE,
    "ミスリルのツルハシ",
    NamedTextColor.GREEN,
    500,
    5,
    370,
  ),
  TITANIUM_PICKAXE(
    "titanium_pickaxe",
    Material.IRON_PICKAXE,
    "チタンのツルハシ",
    NamedTextColor.WHITE,
    600,
    6,
    400,
  ),
  ;

  override val itemStack: ItemStack by lazy {
    val stack = ItemStack(material)
    val miningItem = MiningItem(stack)

    val meta = stack.itemMeta
    meta.displayName(Component.text(displayName, color))
    stack.itemMeta = meta

    miningItem.itemId = id

    val lore = mutableListOf<Component>()
    if (power > 0) lore.add(Component.text("破壊力 $power", NamedTextColor.GRAY))
    if (speed > 0) lore.add(Component.text("採掘速度 +$speed", NamedTextColor.GRAY))
    if (fortune > 0) lore.add(Component.text("採掘運 +$fortune", NamedTextColor.GRAY))

    description.forEach { line -> lore.add(Component.text(line, NamedTextColor.GRAY)) }

    val finalMeta = stack.itemMeta
    finalMeta.lore(lore)
    stack.itemMeta = finalMeta

    stack
  }

  companion object {
    private val byId = entries.associateBy { it.id }

    fun getById(id: String?): PickaxeItem? = byId[id]
  }
}
