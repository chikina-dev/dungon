package net.chikina.minecraft.dungeon.util

object CoordinatePacker {
  /**
   * Packs block coordinates into a single Long. Uses 26 bits for X, 26 bits for Z, and 12 bits for
   * Y. X: [-30M, 30M], Z: [-30M, 30M], Y: [0, 4096]
   */
  fun pack(x: Int, y: Int, z: Int): Long = (x.toLong() and 0x3FFFFFF) or
    ((z.toLong() and 0x3FFFFFF) shl 26) or
    ((y.toLong() and 0xFFF) shl 52)

  fun unpackX(packed: Long): Int {
    var x = (packed and 0x3FFFFFF).toInt()
    if (x >= 0x2000000) x -= 0x4000000
    return x
  }

  fun unpackZ(packed: Long): Int {
    var z = ((packed ushr 26) and 0x3FFFFFF).toInt()
    if (z >= 0x2000000) z -= 0x4000000
    return z
  }

  fun unpackY(packed: Long): Int {
    val y = ((packed ushr 52) and 0xFFF).toInt()
    // Sign extension for 12-bit value (bit 11 is sign)
    // Shift left to alignment with 32-bit int, then arithmetic shift right
    return (y shl 20) shr 20
  }
}
