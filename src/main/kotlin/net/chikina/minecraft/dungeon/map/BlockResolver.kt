package net.chikina.minecraft.dungeon.map

import org.bukkit.Material
import kotlin.math.abs

object BlockResolver {
  fun resolve(
    y: Int,
    env: Environment,
    biome: Biome,
    config: MapConfig,
    variant: Double = 0.0,
  ): Material {
    if (y <= config.minY) return Material.BEDROCK

    val baseAmplitude =
      if (biome == Biome.EXTREME_HILLS || biome == Biome.MOUNTAIN_PEAKS) 120.0 else 60.0

    val maxY = config.maxTerrainY - 1
    val maxAvailableHeight = (maxY - config.baseHeight).toDouble()
    val safeAmplitude = baseAmplitude.coerceAtMost(maxAvailableHeight * 0.9)

    val naturalSurfaceHeight = (config.baseHeight + env.elevation * safeAmplitude).toInt()
    val surfaceHeight = naturalSurfaceHeight.coerceAtMost(maxY)

    if (y > surfaceHeight) {
      return if (y <= config.seaLevel) Material.WATER else Material.AIR
    }

    val depthFromSurface = surfaceHeight - y

    if (depthFromSurface == 0) {
      return if (y < config.seaLevel && biome != Biome.ICE && biome != Biome.MOUNTAIN_PEAKS) {
        when (biome) {
          Biome.MESA -> Material.RED_SAND
          Biome.DESERT -> Material.SAND
          else -> Material.GRAVEL
        }
      } else {
        getTopBlock(biome, y)
      }
    } else if (depthFromSurface < 4) {
      return getSubSurfaceBlock(biome)
    }

    return getUndergroundBlock(env.x.toInt(), y, env.z.toInt(), biome, variant)
  }

  private fun getTopBlock(biome: Biome, y: Int): Material = when (biome) {
    Biome.DESERT -> Material.SAND
    Biome.MESA -> Material.RED_SAND
    Biome.SAVANNA -> Material.GRASS_BLOCK
    Biome.JUNGLE -> Material.GRASS_BLOCK
    Biome.SWAMP -> Material.GRASS_BLOCK
    Biome.PLAINS -> Material.GRASS_BLOCK
    Biome.FOREST, Biome.BIRCH_FOREST -> Material.GRASS_BLOCK
    Biome.TAIGA -> Material.PODZOL
    Biome.EXTREME_HILLS -> if (y > 90) Material.SNOW_BLOCK else Material.STONE
    Biome.MOUNTAIN_PEAKS -> Material.SNOW_BLOCK
    Biome.TUNDRA -> Material.SNOW_BLOCK
    Biome.ICE -> Material.PACKED_ICE
    Biome.OCEAN -> Material.GRAVEL
    Biome.DEEP_OCEAN -> Material.GRAVEL
  }

  private fun getSubSurfaceBlock(biome: Biome): Material = when (biome) {
    Biome.DESERT -> Material.SANDSTONE
    Biome.MESA -> Material.TERRACOTTA
    Biome.EXTREME_HILLS, Biome.MOUNTAIN_PEAKS -> Material.STONE
    else -> Material.DIRT
  }

  private fun getUndergroundBlock(x: Int, y: Int, z: Int, biome: Biome, variant: Double): Material {
    var h = (x * 374761393) xor (y * 668265263) xor (z * 982451653)
    h = (h xor (h ushr 13)) * 1274126177
    h = h xor (h ushr 16)

    val rand = (h and 0x7FFFFFFF) / 2147483647.0

    if (rand < 0.16) return Material.COAL_ORE

    if (y < 64 && rand < 0.17) return Material.IRON_ORE

    if (y < 32) {
      if (biome == Biome.MESA && y > 32 && rand < 0.18) return Material.GOLD_ORE
      if (rand < 0.172) return Material.GOLD_ORE
    }

    if (y < 32 && rand < 0.174) return Material.LAPIS_ORE

    if (y < 16 && rand < 0.178) return Material.REDSTONE_ORE

    if (y < 16 && rand < 0.180) return Material.DIAMOND_ORE

    if ((biome == Biome.EXTREME_HILLS || biome == Biome.MOUNTAIN_PEAKS) && rand < 0.182) {
      return Material.EMERALD_ORE
    }

    if (variant > 0.4) return Material.GRANITE
    if (variant < -0.4) return Material.ANDESITE
    if (abs(variant) < 0.15) return Material.DIORITE

    if (biome == Biome.MESA && y > 32) return Material.TERRACOTTA
    if (biome == Biome.DESERT && rand < 0.3) return Material.SANDSTONE
    if (biome == Biome.ICE || biome == Biome.TUNDRA) {
      if (rand < 0.2) return Material.PACKED_ICE
    }

    return Material.STONE
  }
}
