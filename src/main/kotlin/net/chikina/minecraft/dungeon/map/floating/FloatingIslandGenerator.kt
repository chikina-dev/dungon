package net.chikina.minecraft.dungeon.map.floating

import net.chikina.minecraft.dungeon.map.floating.cave.IslandCaveGenerator
import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import net.chikina.minecraft.dungeon.map.floating.surface.IslandSurfaceGenerator
import net.chikina.minecraft.dungeon.util.AsyncBlockBuffer
import net.chikina.minecraft.dungeon.util.BlockOperationManager
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.concurrent.CompletableFuture

class FloatingIslandGenerator(
  private val config: FloatingIslandConfig,
) {
  private lateinit var surfaceGenerator: IslandSurfaceGenerator
  private val caveGenerator = IslandCaveGenerator(config)

  private val operationManager = BlockOperationManager

  fun generate(world: World) {
    // Run generation asynchronously
    CompletableFuture.runAsync {
      try {
        surfaceGenerator = IslandSurfaceGenerator(config, world.uid)
        val buffer = AsyncBlockBuffer()

        // 1. Generate Surface (Overwrite Mode)
        surfaceGenerator.generate(buffer)

        // 2. Pre-calculate Entrance
        val entranceLocation = surfaceGenerator.determineEntranceLocation()

        // 3. Generate Cave
        if (entranceLocation != null) {
          caveGenerator.generate(buffer, entranceLocation)
        } else {
          Bukkit
            .getLogger()
            .warning("No entrance location found!")
        }

        // 4. Flush
        val batch = buffer.flushToBatch(world)
        operationManager.queueBatch(batch)
      } catch (e: Exception) {
        Bukkit
          .getLogger()
          .severe("Error generating island: ${e.message}")
        e.printStackTrace()
      }
    }
  }
}
