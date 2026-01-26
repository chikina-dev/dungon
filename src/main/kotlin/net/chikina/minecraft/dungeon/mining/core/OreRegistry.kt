package net.chikina.minecraft.dungeon.mining.core

import net.chikina.minecraft.dungeon.item.OreItem
import org.bukkit.Material

data class OreData(val hardness: Double, val tier: Int, val customDrop: OreItem? = null)

object OreRegistry {

    private lateinit var ORE_MAP: Map<Material, OreData>

    fun initialize() {
        ORE_MAP =
                mapOf(
                        Material.COAL_ORE to OreData(3.0, 1),
                        Material.DEEPSLATE_COAL_ORE to OreData(3.0, 1),
                        Material.COPPER_ORE to OreData(6.0, 1),
                        Material.DEEPSLATE_COPPER_ORE to OreData(6.0, 1),
                        Material.NETHER_QUARTZ_ORE to OreData(3.0, 1),
                        Material.IRON_ORE to OreData(9.0, 2),
                        Material.DEEPSLATE_IRON_ORE to OreData(9.0, 2),
                        Material.LAPIS_ORE to OreData(21.0, 2),
                        Material.DEEPSLATE_LAPIS_ORE to OreData(21.0, 2),
                        Material.GOLD_ORE to OreData(12.0, 3),
                        Material.DEEPSLATE_GOLD_ORE to OreData(12.0, 3),
                        Material.REDSTONE_ORE to OreData(15.0, 3),
                        Material.DEEPSLATE_REDSTONE_ORE to OreData(15.0, 3),
                        Material.EMERALD_ORE to OreData(18.0, 3),
                        Material.DEEPSLATE_EMERALD_ORE to OreData(18.0, 3),
                        Material.DIAMOND_ORE to OreData(24.0, 4),
                        Material.DEEPSLATE_DIAMOND_ORE to OreData(24.0, 4),
                        Material.ANCIENT_DEBRIS to OreData(384.0, 4),
                        Material.LIGHT_BLUE_WOOL to OreData(48.0, 4, OreItem.MITHRIL_ORE)
                )
    }

    fun getOreData(material: Material): OreData? {
        if (!::ORE_MAP.isInitialized) return null
        return ORE_MAP[material]
    }

    fun isOre(material: Material): Boolean {
        if (!::ORE_MAP.isInitialized) return false
        return ORE_MAP.containsKey(material)
    }
}
