package net.chikina.minecraft.dungeon.map.structure

import net.chikina.minecraft.dungeon.map.Environment
import org.bukkit.Material

open class DungeonStructure(
  val width: Int,
  val height: Int,
  val depth: Int,
  val palette: List<Material>,
  val blocks: IntArray,
  val rules: PlacementRules,
) {
  fun getBlock(x: Int, y: Int, z: Int): Material {
    if (x !in 0 until width || y !in 0 until height || z !in 0 until depth) {
      return Material.AIR
    }
    val index = (y * depth + z) * width + x
    val paletteIndex = blocks[index]
    return if (paletteIndex in palette.indices) palette[paletteIndex] else Material.AIR
  }

  fun getFootprint(): List<Pair<Int, Int>> {
    val footprint = mutableListOf<Pair<Int, Int>>()

    for (y in 0 until height) {
      var hasBlockInLayer = false
      for (z in 0 until depth) {
        for (x in 0 until width) {
          if (getBlock(x, y, z) != Material.AIR) {
            hasBlockInLayer = true
            footprint.add(x to z)
          }
        }
      }
      if (hasBlockInLayer) {
        return footprint
      }
    }
    return emptyList()
  }

  data class PlacementRules(
    val conditions: List<Condition>,
    val spawnChance: Double = 1.0,
    val zIndex: Int = 0,
    val minSpacing: Int = 0,
  )

  interface Condition {
    fun isValid(env: Environment, y: Int, seaLevel: Int): Boolean
  }

  class MinElevation(
    private val min: Double,
  ) : Condition {
    override fun isValid(env: Environment, y: Int, seaLevel: Int): Boolean = env.elevation >= min
  }

  class MaxElevation(
    private val max: Double,
  ) : Condition {
    override fun isValid(env: Environment, y: Int, seaLevel: Int): Boolean = env.elevation <= max
  }

  class EnvironmentRange(
    private val minTemp: Double = -1.0,
    private val maxTemp: Double = 1.0,
    private val minHum: Double = -1.0,
    private val maxHum: Double = 1.0,
  ) : Condition {
    override fun isValid(env: Environment, y: Int, seaLevel: Int): Boolean =
      env.temperature in minTemp..maxTemp && env.humidity in minHum..maxHum
  }

  class MinHeightAboveSeaLevel(
    private val minOffset: Int = 1,
  ) : Condition {
    override fun isValid(env: Environment, y: Int, seaLevel: Int): Boolean = y >= seaLevel + minOffset
  }
}
