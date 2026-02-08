package net.chikina.minecraft.dungeon.gathering

import net.chikina.minecraft.dungeon.item.OreItem
import org.bukkit.Material

enum class GatherableType(
  val material: Material,
  val hardness: Double,
  val tier: Int,
  val toolType: ToolType, // AXE or PICKAXE
  val customDrop: OreItem? = null,
) {
  // --- Ores (Pickaxe) ---
  COAL_ORE(Material.COAL_ORE, 3.0, 1, ToolType.PICKAXE),
  DEEPSLATE_COAL_ORE(Material.DEEPSLATE_COAL_ORE, 3.0, 1, ToolType.PICKAXE),
  COPPER_ORE(Material.COPPER_ORE, 6.0, 1, ToolType.PICKAXE),
  DEEPSLATE_COPPER_ORE(Material.DEEPSLATE_COPPER_ORE, 6.0, 1, ToolType.PICKAXE),
  NETHER_QUARTZ_ORE(Material.NETHER_QUARTZ_ORE, 3.0, 1, ToolType.PICKAXE),
  IRON_ORE(Material.IRON_ORE, 9.0, 2, ToolType.PICKAXE),
  DEEPSLATE_IRON_ORE(Material.DEEPSLATE_IRON_ORE, 9.0, 2, ToolType.PICKAXE),
  LAPIS_ORE(Material.LAPIS_ORE, 21.0, 2, ToolType.PICKAXE),
  DEEPSLATE_LAPIS_ORE(Material.DEEPSLATE_LAPIS_ORE, 21.0, 2, ToolType.PICKAXE),
  GOLD_ORE(Material.GOLD_ORE, 12.0, 3, ToolType.PICKAXE),
  DEEPSLATE_GOLD_ORE(Material.DEEPSLATE_GOLD_ORE, 12.0, 3, ToolType.PICKAXE),
  REDSTONE_ORE(Material.REDSTONE_ORE, 15.0, 3, ToolType.PICKAXE),
  DEEPSLATE_REDSTONE_ORE(Material.DEEPSLATE_REDSTONE_ORE, 15.0, 3, ToolType.PICKAXE),
  EMERALD_ORE(Material.EMERALD_ORE, 18.0, 3, ToolType.PICKAXE),
  DEEPSLATE_EMERALD_ORE(Material.DEEPSLATE_EMERALD_ORE, 18.0, 3, ToolType.PICKAXE),
  DIAMOND_ORE(Material.DIAMOND_ORE, 24.0, 4, ToolType.PICKAXE),
  DEEPSLATE_DIAMOND_ORE(Material.DEEPSLATE_DIAMOND_ORE, 24.0, 4, ToolType.PICKAXE),
  ANCIENT_DEBRIS(Material.ANCIENT_DEBRIS, 384.0, 4, ToolType.PICKAXE),
  LIGHT_BLUE_WOOL(Material.LIGHT_BLUE_WOOL, 48.0, 4, ToolType.PICKAXE, OreItem.MITHRIL_ORE),

  // --- Logs (Axe) ---
  OAK_LOG(Material.OAK_LOG, 2.0, 1, ToolType.AXE),
  SPRUCE_LOG(Material.SPRUCE_LOG, 2.0, 1, ToolType.AXE),
  BIRCH_LOG(Material.BIRCH_LOG, 2.0, 1, ToolType.AXE),
  JUNGLE_LOG(Material.JUNGLE_LOG, 2.0, 1, ToolType.AXE),
  ACACIA_LOG(Material.ACACIA_LOG, 2.0, 1, ToolType.AXE),
  DARK_OAK_LOG(Material.DARK_OAK_LOG, 2.0, 1, ToolType.AXE),
  MANGROVE_LOG(Material.MANGROVE_LOG, 2.0, 1, ToolType.AXE),
  CHERRY_LOG(Material.CHERRY_LOG, 2.0, 1, ToolType.AXE),
  ;

  companion object {
    private val BY_MATERIAL = entries.associateBy { it.material }

    fun fromMaterial(material: Material): GatherableType? = BY_MATERIAL[material]
  }
}

enum class ToolType {
  PICKAXE,
  AXE,
  SHOVEL,
  HOE,
}
