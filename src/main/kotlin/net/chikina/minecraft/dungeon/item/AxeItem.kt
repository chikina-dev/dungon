package net.chikina.minecraft.dungeon.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class AxeItem(
  val id: String,
  val material: Material,
  val displayName: String,
  val color: NamedTextColor,
  val speed: Int,
  val power: Int, // Foraging Power
  val fortune: Int, // Foraging Fortune
  val description: List<String> = emptyList(),
) : GameItem {
  WOODEN_AXE("wooden_axe", Material.WOODEN_AXE, "木の斧", NamedTextColor.GREEN, 100, 1, 0),
  COPPER_AXE("copper_axe", Material.STONE_AXE, "銅の斧", NamedTextColor.GREEN, 150, 2, 0),
  IRON_AXE("iron_axe", Material.IRON_AXE, "鉄の斧", NamedTextColor.GREEN, 200, 3, 0),
  GOLD_AXE("gold_axe", Material.GOLDEN_AXE, "金の斧", NamedTextColor.GREEN, 250, 3, 0),
  DIAMOND_AXE("diamond_axe", Material.DIAMOND_AXE, "ダイヤモンドの斧", NamedTextColor.GREEN, 300, 4, 0),
  MITHRIL_AXE("mithril_axe", Material.DIAMOND_AXE, "ミスリルの斧", NamedTextColor.GREEN, 500, 5, 370),
  TITANIUM_AXE("titanium_axe", Material.IRON_AXE, "チタンの斧", NamedTextColor.WHITE, 600, 6, 400),
  ;

  override val itemStack: ItemStack by lazy {
    val stack = ItemStack(material)
    val miningItem = MiningItem(stack)

    val meta = stack.itemMeta
    meta.displayName(Component.text(displayName, color))
    stack.itemMeta = meta

    miningItem.itemId = id

    val lore = mutableListOf<Component>()
    if (power > 0) lore.add(Component.text("伐採力 $power", NamedTextColor.GRAY))
    if (speed > 0) lore.add(Component.text("伐採速度 +$speed", NamedTextColor.GRAY))
    if (fortune > 0) lore.add(Component.text("伐採運 +$fortune", NamedTextColor.GRAY))

    description.forEach { line -> lore.add(Component.text(line, NamedTextColor.GRAY)) }

    val finalMeta = stack.itemMeta
    finalMeta.lore(lore)
    stack.itemMeta = finalMeta

    stack
  }

  companion object {
    private val byId = entries.associateBy { it.id }

    fun getById(id: String?): AxeItem? = byId[id]
  }
}
