package net.chikina.minecraft.dungeon.util

import org.bukkit.Material
import org.bukkit.World
import java.util.concurrent.ConcurrentHashMap

class AsyncBlockBuffer {
  // Key: ChunkKey (Long), Value: Map<BlockIndex (Int), Material>
  // Using ConcurrentHashMap for thread safety across chunks
  private val chunks = ConcurrentHashMap<Long, MutableMap<Int, Material>>()

  fun setBlock(x: Int, y: Int, z: Int, material: Material) {
    val chunkKey = getChunkKey(x shr 4, z shr 4)

    // We use a compute pattern to ensure thread safety when creating the map,
    // but for high performance writing within a chunk, we might want synchronization
    // if multiple threads write to the SAME chunk.
    // However, generally generation is spatially partitioned.
    // For absolute safety:
    chunks.compute(chunkKey) { _, map ->
      val chunkMap = map ?: HashMap()
      synchronized(chunkMap) { chunkMap[getBlockIndex(x and 15, y, z and 15)] = material }
      chunkMap
    }
  }

  fun getBlock(x: Int, y: Int, z: Int): Material {
    val chunkKey = getChunkKey(x shr 4, z shr 4)
    val map = chunks[chunkKey] ?: return Material.AIR

    synchronized(map) {
      return map[getBlockIndex(x and 15, y, z and 15)] ?: Material.AIR
    }
  }

  fun flushToBatch(world: World): BlockBatch {
    // Instead of flattening, we pass the raw chunks map structure
    // This saves memory and CPU time
    val chunksCopy = HashMap<Long, Map<Int, Material>>()
    for ((key, value) in chunks) {
      synchronized(value) {
        if (value.isNotEmpty()) {
          chunksCopy[key] = HashMap(value)
        }
      }
    }
    return BlockBatch(world, chunksCopy)
  }

  companion object {
    fun getChunkKey(x: Int, z: Int): Long = (x.toLong() and 0xFFFFFFFFL) or (z.toLong() shl 32)

    fun getBlockIndex(x: Int, y: Int, z: Int): Int {
      // FIX: Original logic had Z overlapping Y.
      // New Layout: [X:4 bits @ 16] [Z:4 bits @ 12] [Y:12 bits @ 0]
      // X: 0-15 (4 bits)
      // Z: 0-15 (4 bits)
      // Y: 0-4095 (12 bits)
      return (x shl 16) or (z shl 12) or (y and 0xFFF)
    }
  }
}
