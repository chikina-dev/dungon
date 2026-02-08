package net.chikina.minecraft.dungeon.foraging

import org.bukkit.Material

enum class TreeType(
  val log: Material,
  val leaves: Material,
) {
  OAK(Material.OAK_LOG, Material.OAK_LEAVES),
  BIRCH(Material.BIRCH_LOG, Material.BIRCH_LEAVES),
  SPRUCE(Material.SPRUCE_LOG, Material.SPRUCE_LEAVES),
  JUNGLE(Material.JUNGLE_LOG, Material.JUNGLE_LEAVES),
  ACACIA(Material.ACACIA_LOG, Material.ACACIA_LEAVES),
  DARK_OAK(Material.DARK_OAK_LOG, Material.DARK_OAK_LEAVES),
}
