package net.chikina.minecraft.dungeon.util

import org.bukkit.Material
import org.bukkit.World
import java.util.concurrent.ConcurrentLinkedQueue

object BlockOperationManager {
  private val queue = ConcurrentLinkedQueue<Batch>()
  private const val BLOCKS_PER_TICK = 50000 // Increased from 10k for faster generation

  fun queueBatch(batch: Batch) {
    queue.add(batch)
  }

  fun start() {
    DungeonTask.runTimer(1L, 1L) { processQueue() }
  }

  private fun processQueue() {
    if (queue.isEmpty()) return

    var blocksProcessed = 0
    val startTime = System.currentTimeMillis()

    while (!queue.isEmpty() && blocksProcessed < BLOCKS_PER_TICK) {
      val batch = queue.peek() // Don't remove yet, might not finish
      if (batch == null) break

      val processedInBatch = batch.apply(BLOCKS_PER_TICK - blocksProcessed)
      blocksProcessed += processedInBatch

      if (batch.isFinished()) {
        queue.poll() // Remove completed batch
      }
    }
  }
}

interface Batch {
  fun apply(limit: Int): Int

  fun isFinished(): Boolean
}

class RegionClearBatch(
  private val world: World,
  private val minX: Int,
  private val minY: Int,
  private val minZ: Int,
  private val maxX: Int,
  private val maxY: Int,
  private val maxZ: Int,
) : Batch {
  private var currentX = minX
  private var currentY = minY
  private var currentZ = minZ
  private var finished = false

  override fun apply(limit: Int): Int {
    var count = 0

    // Cache chunk lookup
    var cachedChunk: org.bukkit.Chunk? = null
    var cachedChunkX = Int.MIN_VALUE
    var cachedChunkZ = Int.MIN_VALUE

    // Iterate Z then X then Y to match memory/chunk layout better
    // Actually Bukkit/MC stores sections Y-first usually, but Chunks are X/Z.
    // Let's iterate Chunks (X/Z) then Y.
    // Current state tracking updates (X, Y, Z).

    // Revised Loop: X -> Z -> Y
    while (count < limit && !finished) {
      val cx = currentX shr 4
      val cz = currentZ shr 4

      if (cachedChunk == null || cachedChunkX != cx || cachedChunkZ != cz) {
        if (world.isChunkLoaded(cx, cz)) {
          cachedChunk = world.getChunkAt(cx, cz)
        } else {
          // If chunk is not loaded, we can technically skip setting AIR if it's new generation,
          // but to be safe we should load it.
          // For performance, getting an unloaded chunk is expensive.
          // Assuming generation happens in loaded area.
          cachedChunk = world.getChunkAt(cx, cz)
        }
        cachedChunkX = cx
        cachedChunkZ = cz
      }

      // Check block
      val block = cachedChunk!!.getBlock(currentX and 15, currentY, currentZ and 15)
      if (block.type != Material.AIR) {
        block.setType(Material.AIR, false)
      }
      count++

      // Advance
      currentY++
      if (currentY > maxY) {
        currentY = minY
        currentZ++
        if (currentZ > maxZ) {
          currentZ = minZ
          currentX++
          if (currentX > maxX) {
            finished = true
          }
        }
      }
    }
    return count
  }

  override fun isFinished(): Boolean = finished
}

class BlockBatch(
  private val world: World,
  private val chunks: Map<Long, Map<Int, Material>>,
) : Batch {
  private val chunkIterator = chunks.iterator()
  private var currentChunk: Map.Entry<Long, Map<Int, Material>>? = null
  private var blockIterator: Iterator<Map.Entry<Int, Material>>? = null

  private var isFinished = false

  // Cache for current chunk coordinates
  private var currentCX = 0
  private var currentCZ = 0

  init {
    advanceChunk()
  }

  private fun advanceChunk() {
    if (chunkIterator.hasNext()) {
      val entry = chunkIterator.next()
      currentChunk = entry
      blockIterator = entry.value.iterator()

      val chunkKey = entry.key
      currentCX = (chunkKey.toLong() and 0xFFFFFFFFL).toInt()
      currentCZ = (chunkKey.toLong() ushr 32).toInt()
    } else {
      currentChunk = null
      blockIterator = null
      isFinished = true
    }
  }

  override fun apply(limit: Int): Int {
    var count = 0
    var cachedChunk: org.bukkit.Chunk? = null
    var cachedChunkX = Int.MIN_VALUE
    var cachedChunkZ = Int.MIN_VALUE

    while (count < limit && !isFinished) {
      if (blockIterator != null && blockIterator!!.hasNext()) {
        val entry = blockIterator!!.next()
        val index = entry.key
        val material = entry.value

        // Unpack index: [X:4 @ 16] [Z:4 @ 12] [Y:12 @ 0]
        val lx = (index shr 16) and 0xF
        val lz = (index shr 12) and 0xF
        val ly = index and 0xFFF

        // Current Batch Chunk Coordinates (from advanceChunk)
        val cx = currentCX
        val cz = currentCZ

        // Ensure we have the Bukkit Chunk object cached
        if (cachedChunk == null || cachedChunkX != cx || cachedChunkZ != cz) {
          // Force load or get
          cachedChunk = world.getChunkAt(cx, cz)
          cachedChunkX = cx
          cachedChunkZ = cz
        }

        val y = if (ly > 2048) ly - 4096 else ly // Handle negative Y

        // Use chunk.getBlock which is faster than world.getBlockAt (skips chunk lookup)
        val block = cachedChunk!!.getBlock(lx, y, lz)
        if (block.type != material) {
          block.setType(material, false)
        }
        count++
      } else {
        advanceChunk()
        // Invalidate cache on chunk switch (though logic handles it via X/Z check)
        cachedChunk = null
      }
    }
    return count
  }

  override fun isFinished(): Boolean = isFinished

  fun size(): Int {
    var s = 0
    for (map in chunks.values) {
      s += map.size
    }
    return s
  }
}
