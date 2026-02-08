package net.chikina.minecraft.dungeon.map

class Region(
  val x: Int,
  val z: Int,
) {
  private val chunks = arrayOfNulls<Chunk>(MapConstants.REGION_WIDTH * MapConstants.REGION_HEIGHT)

  fun getChunk(localChunkX: Int, localChunkZ: Int): Chunk? {
    if (localChunkX !in 0 until MapConstants.REGION_WIDTH ||
      localChunkZ !in 0 until MapConstants.REGION_HEIGHT
    ) {
      return null
    }
    return chunks[localChunkZ * MapConstants.REGION_WIDTH + localChunkX]
  }

  fun setChunk(localChunkX: Int, localChunkZ: Int, chunk: Chunk) {
    if (localChunkX !in 0 until MapConstants.REGION_WIDTH ||
      localChunkZ !in 0 until MapConstants.REGION_HEIGHT
    ) {
      return
    }
    chunks[localChunkZ * MapConstants.REGION_WIDTH + localChunkX] = chunk
  }
}
