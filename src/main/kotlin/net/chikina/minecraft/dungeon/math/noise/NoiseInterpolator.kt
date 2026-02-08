package net.chikina.minecraft.dungeon.math.noise

import net.chikina.minecraft.dungeon.util.Region3D

class NoiseInterpolator(
  private val noise: Perlin,
  private val region: Region3D,
  private val cellSize: Int, // e.g. 4
  private val scaleX: Double,
  private val scaleY: Double,
  private val scaleZ: Double,
  private val amp: Double = 1.0,
  private val offsetX: Double = 0.0,
  private val offsetY: Double = 0.0,
  private val offsetZ: Double = 0.0,
) {
  private val xSize = (region.width / cellSize) + 1
  private val ySize = (region.height / cellSize) + 1
  private val zSize = (region.depth / cellSize) + 1
  private val noiseSamples = DoubleArray(xSize * ySize * zSize)

  init {
    fillSamples()
  }

  private fun fillSamples() {
    for (x in 0 until xSize) {
      for (y in 0 until ySize) {
        for (z in 0 until zSize) {
          val worldX = region.minX + (x * cellSize)
          val worldY = region.minY + (y * cellSize)
          val worldZ = region.minZ + (z * cellSize)

          val nx = worldX * scaleX + offsetX
          val ny = worldY * scaleY + offsetY
          val nz = worldZ * scaleZ + offsetZ

          noiseSamples[index(x, y, z)] = noise.noise(nx, ny, nz) * amp
        }
      }
    }
  }

  fun getNoise(x: Int, y: Int, z: Int): Double {
    val localX = x - region.minX
    val localY = y - region.minY
    val localZ = z - region.minZ

    val cellX = localX / cellSize
    val cellY = localY / cellSize
    val cellZ = localZ / cellSize

    if (cellX >= xSize - 1 ||
      cellY >= ySize - 1 ||
      cellZ >= zSize - 1 ||
      cellX < 0 ||
      cellY < 0 ||
      cellZ < 0
    ) {
      // Fallback for out of bounds (shouldn't happen if region matches)
      return noise.noise(x * scaleX + offsetX, y * scaleY + offsetY, z * scaleZ + offsetZ) * amp
    }

    val xFrac = (localX % cellSize).toDouble() / cellSize
    val yFrac = (localY % cellSize).toDouble() / cellSize
    val zFrac = (localZ % cellSize).toDouble() / cellSize

    val n000 = noiseSamples[index(cellX, cellY, cellZ)]
    val n100 = noiseSamples[index(cellX + 1, cellY, cellZ)]
    val n010 = noiseSamples[index(cellX, cellY + 1, cellZ)]
    val n110 = noiseSamples[index(cellX + 1, cellY + 1, cellZ)]
    val n001 = noiseSamples[index(cellX, cellY, cellZ + 1)]
    val n101 = noiseSamples[index(cellX + 1, cellY, cellZ + 1)]
    val n011 = noiseSamples[index(cellX, cellY + 1, cellZ + 1)]
    val n111 = noiseSamples[index(cellX + 1, cellY + 1, cellZ + 1)]

    return lerp3(xFrac, yFrac, zFrac, n000, n100, n010, n110, n001, n101, n011, n111)
  }

  private fun index(x: Int, y: Int, z: Int): Int = (x * ySize * zSize) + (y * zSize) + z

  private fun lerp(t: Double, a: Double, b: Double): Double = a + t * (b - a)

  private fun lerp3(
    x: Double,
    y: Double,
    z: Double,
    n000: Double,
    n100: Double,
    n010: Double,
    n110: Double,
    n001: Double,
    n101: Double,
    n011: Double,
    n111: Double,
  ): Double = lerp(
    z,
    lerp(y, lerp(x, n000, n100), lerp(x, n010, n110)),
    lerp(y, lerp(x, n001, n101), lerp(x, n011, n111)),
  )
}
