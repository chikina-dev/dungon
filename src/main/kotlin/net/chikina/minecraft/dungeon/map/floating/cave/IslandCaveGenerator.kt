package net.chikina.minecraft.dungeon.map.floating.cave

import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import org.bukkit.Location

class IslandCaveGenerator(
  private val config: FloatingIslandConfig,
) {
  private val graphGenerator = CaveGraphGenerator(config)
  private val carver = CaveCarver(config)
  private val populator = CavePopulator(config)

  fun generate(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    entranceLocation: Location,
  ) {
    // 1. Generate Graph
    val graph = graphGenerator.generate(entranceLocation.toVector())

    // 2. Carve Cave
    carver.carving(buffer, graph)

    // 3. Populate
    populator.populate(buffer, graph)
  }
}
