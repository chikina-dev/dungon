package net.chikina.minecraft.dungeon.util

import net.chikina.minecraft.dungeon.Dungeon
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

object BlockOperationManager {
  private val queue = ConcurrentLinkedQueue<Batch>()

  fun queueBatch(batch: Batch) {
    queue.add(batch)
  }

  fun start() {
    DungeonTask.runTimer(1L, 1L) { processQueue() }
  }

  private fun processQueue() {
    val batch = queue.peek() ?: return

    if (!batch.isStarted()) {
      batch.start()
    }

    if (batch.isFinished()) {
      queue.poll()
    }
  }
}

interface Batch {
  fun start()

  fun isStarted(): Boolean

  fun isFinished(): Boolean
}

class BlockBatch(
  private val world: World,
  private val chunks: Map<Long, Map<Int, Material>>,
) : Batch {
  private var finished = false
  private var started = false

  private val totalChunks = chunks.size
  private val completedChunks = AtomicInteger(0)

  override fun start() {
    if (started) return
    started = true

    val plugin = Dungeon.instance

    for ((chunkKey, blockMap) in chunks) {
      val cx = (chunkKey.toLong() and 0xFFFFFFFFL).toInt()
      val cz = (chunkKey.toLong() ushr 32).toInt()

      // Schedule on Region Thread
      Bukkit.getRegionScheduler().execute(plugin, world, cx, cz) {
        applyToChunk(cx, cz, blockMap)
        if (completedChunks.incrementAndGet() >= totalChunks) {
          finished = true
        }
      }
    }
  }

  private fun applyToChunk(cx: Int, cz: Int, blockMap: Map<Int, Material>) {
    // We are on the region thread, so getChunkAt is safe and fast
    val chunk = world.getChunkAt(cx, cz)

    for ((index, material) in blockMap) {
      // Unpack index: [X:4 @ 16] [Z:4 @ 12] [Y:12 @ 0]
      val lx = (index shr 16) and 0xF
      val lz = (index shr 12) and 0xF
      val ly = index and 0xFFF
      val y = if (ly > 2048) ly - 4096 else ly

      val block = chunk.getBlock(lx, y, lz)
      if (block.type != material) {
        block.setType(material, false)
      }
    }
  }

  override fun isStarted(): Boolean = started

  override fun isFinished(): Boolean = finished

  fun size(): Int {
    var s = 0
    for (map in chunks.values) {
      s += map.size
    }
    return s
  }
}
