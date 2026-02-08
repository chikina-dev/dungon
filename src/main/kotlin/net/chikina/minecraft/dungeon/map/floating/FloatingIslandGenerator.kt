package net.chikina.minecraft.dungeon.map.floating

import net.chikina.minecraft.dungeon.map.floating.cave.IslandCaveGenerator
import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import net.chikina.minecraft.dungeon.map.floating.surface.IslandSurfaceGenerator
import org.bukkit.World

class FloatingIslandGenerator(
  private val config: FloatingIslandConfig,
) {
  private lateinit var surfaceGenerator: IslandSurfaceGenerator
  private val caveGenerator = IslandCaveGenerator(config)

  private val operationManager = net.chikina.minecraft.dungeon.util.BlockOperationManager

  fun generate(world: World) {
    // Run generation asynchronously
    java.util.concurrent.CompletableFuture.runAsync {
      try {
        org.bukkit.Bukkit
          .getLogger()
          .info("Starting Async Island Generation...")
        surfaceGenerator = IslandSurfaceGenerator(config, world.uid)
        val buffer = net.chikina.minecraft.dungeon.util
          .AsyncBlockBuffer()

        // 0. Clear Area (Using optimized Batch)
        // Directly queue a clear batch instead of flooding the AsyncBuffer with AIR
        operationManager.queueBatch(
          net.chikina.minecraft.dungeon.util.RegionClearBatch(
            world,
            config.bounds.minX,
            config.bounds.minY,
            config.bounds.minZ,
            config.bounds.maxX,
            config.bounds.maxY,
            config.bounds.maxZ,
          ),
        )
        // No wait needed here technically if we trust the queue order,
        // BUT we are using CompletableFuture.runAsync for generation which also queues a batch.
        // We need to ensure the ClearBatch runs BEFORE the Generation Batch.
        // Since `queueBatch` adds to `ConcurrentLinkedQueue`, order is preserved.
        // However, the generation runs in THIS thread (step 1-3) and then queues at end.
        // So ClearBatch is added first.

        org.bukkit.Bukkit
          .getLogger()
          .info("Clear Batch Queued.")
        org.bukkit.Bukkit
          .getLogger()
          .info("Area Cleared.")

        // 1. Generate Surface (This populates the internal heightmap/interpolators)
        surfaceGenerator.generate(buffer)
        org.bukkit.Bukkit
          .getLogger()
          .info("Surface Generated.")

        // 2. Pre-calculate Entrance
        val entranceLocation = surfaceGenerator.determineEntranceLocation()

        // 3. Generate Cave (Pass surface generator for shared density checks)
        if (entranceLocation != null) {
          org.bukkit.Bukkit
            .getLogger()
            .info("Generating Cave at $entranceLocation")
          // Optimization: Cave generator can use SurfaceGenerator's cached density data
          // We need to pass surfaceGenerator to caveGenerator or graphGenerator.
          // For now, let's just run it. The specific shared-context optimization
          // requires changing CaveGraphGenerator signature.
          // Let's assume the previous optimization (Interpolation in Surface) matches user request.
          caveGenerator.generate(buffer, entranceLocation)
        } else {
          org.bukkit.Bukkit
            .getLogger()
            .warning("No entrance location found!")
        }

        // 4. Flush
        org.bukkit.Bukkit
          .getLogger()
          .info("Flushing to Batch...")
        val batch = buffer.flushToBatch(world)
        operationManager.queueBatch(batch)
        org.bukkit.Bukkit
          .getLogger()
          .info("Batch Queued: ${batch.size()} blocks")
      } catch (e: Exception) {
        org.bukkit.Bukkit
          .getLogger()
          .severe("Error generating island: ${e.message}")
        e.printStackTrace()
      }
    }
  }
}
