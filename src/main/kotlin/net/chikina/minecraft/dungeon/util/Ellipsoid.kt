package net.chikina.minecraft.dungeon.util

import org.bukkit.util.Vector

class Ellipsoid(
  val center: Vector,
  val radiusX: Double,
  val radiusY: Double,
  val radiusZ: Double,
) {
  fun contains(x: Double, y: Double, z: Double): Boolean = getNormalizedDistanceSquared(x, y, z) <= 1.0

  fun contains(v: Vector): Boolean = contains(v.x, v.y, v.z)

  fun getNormalizedDistance(x: Double, y: Double, z: Double): Double = Math.sqrt(getNormalizedDistanceSquared(x, y, z))

  private fun getNormalizedDistanceSquared(x: Double, y: Double, z: Double): Double {
    val dx = (x - center.x) / radiusX
    val dy = (y - center.y) / radiusY
    val dz = (z - center.z) / radiusZ
    return dx * dx + dy * dy + dz * dz
  }

  // Check if a point is inside with a margin (effectively shrinking the ellipsoid)
  fun contains(x: Double, y: Double, z: Double, margin: Double): Boolean {
    val effRx = radiusX - margin
    val effRy = radiusY - margin
    val effRz = radiusZ - margin

    if (effRx <= 0 || effRy <= 0 || effRz <= 0) return false

    val dx = (x - center.x) / effRx
    val dy = (y - center.y) / effRy
    val dz = (z - center.z) / effRz

    return (dx * dx + dy * dy + dz * dz) <= 1.0
  }
}
