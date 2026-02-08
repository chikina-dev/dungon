package net.chikina.minecraft.dungeon.map

enum class Biome {
  DESERT,
  MESA,
  SAVANNA,
  JUNGLE,
  SWAMP,
  PLAINS,
  FOREST,
  BIRCH_FOREST,
  TAIGA,
  EXTREME_HILLS,
  TUNDRA,
  ICE,
  OCEAN,
  DEEP_OCEAN,
  MOUNTAIN_PEAKS,
  ;

  companion object {
    fun resolve(temperature: Double, humidity: Double, elevation: Double): Biome {
      if (elevation < -0.6) return DEEP_OCEAN
      if (elevation < -0.2) return OCEAN

      if (elevation > 0.8) return MOUNTAIN_PEAKS
      if (elevation > 0.5) return EXTREME_HILLS

      return when {
        temperature < -0.5 -> {
          when {
            humidity < -0.5 -> ICE
            humidity < 0.0 -> TUNDRA
            else -> TAIGA
          }
        }

        temperature < 0.0 -> {
          when {
            humidity < -0.6 -> EXTREME_HILLS
            humidity < -0.2 -> TAIGA
            humidity < 0.3 -> PLAINS
            else -> FOREST
          }
        }

        temperature < 0.5 -> {
          when {
            humidity < -0.5 -> PLAINS
            humidity < 0.0 -> BIRCH_FOREST
            humidity < 0.4 -> SWAMP
            else -> JUNGLE
          }
        }

        else -> {
          when {
            humidity < -0.6 -> DESERT
            humidity < -0.2 -> MESA
            humidity < 0.3 -> SAVANNA
            else -> JUNGLE
          }
        }
      }
    }
  }
}
