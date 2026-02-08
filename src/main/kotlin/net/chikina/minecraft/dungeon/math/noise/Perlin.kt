package net.chikina.minecraft.dungeon.math.noise

import java.util.Random
import kotlin.math.floor

class Perlin(
  seed: Long,
) {
  private val permutation = IntArray(512)

  init {
    val p = IntArray(256) { it }
    val random = Random(seed)

    for (i in 0 until 256) {
      val j = random.nextInt(256 - i) + i
      val temp = p[i]
      p[i] = p[j]
      p[j] = temp

      permutation[i] = p[i]
      permutation[i + 256] = p[i]
    }
  }

  fun noise(x: Double, y: Double, z: Double): Double {
    val fx = floor(x).toInt()
    val fy = floor(y).toInt()
    val fz = floor(z).toInt()

    val xMask = fx and 255
    val yMask = fy and 255
    val zMask = fz and 255

    val xRel = x - fx
    val yRel = y - fy
    val zRel = z - fz

    val u = fade(xRel)
    val v = fade(yRel)
    val w = fade(zRel)

    val a = permutation[xMask] + yMask
    val aa = permutation[a] + zMask
    val ab = permutation[a + 1] + zMask
    val b = permutation[xMask + 1] + yMask
    val ba = permutation[b] + zMask
    val bb = permutation[b + 1] + zMask

    return lerp(
      w,
      lerp(
        v,
        lerp(
          u,
          grad(permutation[aa], xRel, yRel, zRel),
          grad(permutation[ba], xRel - 1, yRel, zRel),
        ),
        lerp(
          u,
          grad(permutation[ab], xRel, yRel - 1, zRel),
          grad(permutation[bb], xRel - 1, yRel - 1, zRel),
        ),
      ),
      lerp(
        v,
        lerp(
          u,
          grad(permutation[aa + 1], xRel, yRel, zRel - 1),
          grad(permutation[ba + 1], xRel - 1, yRel, zRel - 1),
        ),
        lerp(
          u,
          grad(permutation[ab + 1], xRel, yRel - 1, zRel - 1),
          grad(permutation[bb + 1], xRel - 1, yRel - 1, zRel - 1),
        ),
      ),
    )
  }

  private fun fade(t: Double): Double = t * t * t * (t * (t * 6 - 15) + 10)

  private fun lerp(t: Double, a: Double, b: Double): Double = a + t * (b - a)

  private fun grad(hash: Int, x: Double, y: Double, z: Double): Double {
    val h = hash and 15
    val u = if (h < 8) x else y
    val v = if (h < 4) {
      y
    } else if (h == 12 || h == 14) {
      x
    } else {
      z
    }
    return (if (h and 1 == 0) u else -u) + (if (h and 2 == 0) v else -v)
  }
}
