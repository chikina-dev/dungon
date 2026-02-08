package net.chikina.minecraft.dungeon.map

class Chunk(
  val x: Int,
  val z: Int,
  val top: Int,
  val minY: Int = 0,
) {
  val height = top - minY
  val blocks = IntArray(MapConstants.CHUNK_WIDTH * MapConstants.CHUNK_HEIGHT * height)

  fun getBlock(localX: Int, localZ: Int, y: Int): Int {
    if (localX !in 0 until MapConstants.CHUNK_WIDTH ||
      localZ !in 0 until MapConstants.CHUNK_HEIGHT ||
      y !in minY until top
    ) {
      return 0
    }
    val relativeY = y - minY
    val index = (relativeY * MapConstants.CHUNK_HEIGHT + localZ) * MapConstants.CHUNK_WIDTH + localX
    return blocks[index]
  }

  fun setBlock(localX: Int, localZ: Int, y: Int, value: Int) {
    if (localX !in 0 until MapConstants.CHUNK_WIDTH ||
      localZ !in 0 until MapConstants.CHUNK_HEIGHT ||
      y !in minY until top
    ) {
      return
    }
    val relativeY = y - minY
    val index = (relativeY * MapConstants.CHUNK_HEIGHT + localZ) * MapConstants.CHUNK_WIDTH + localX
    blocks[index] = value
  }
}
