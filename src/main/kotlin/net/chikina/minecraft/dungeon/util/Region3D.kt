package net.chikina.minecraft.dungeon.util

import org.bukkit.util.Vector

data class Region3D(
  val minX: Int,
  val minY: Int,
  val minZ: Int,
  val maxX: Int,
  val maxY: Int,
  val maxZ: Int,
) {
  val width: Int
    get() = maxX - minX + 1
  val height: Int
    get() = maxY - minY + 1
  val depth: Int
    get() = maxZ - minZ + 1

  val center: Vector
    get() = Vector((minX + maxX) / 2.0, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0)

  fun forEach(action: (x: Int, y: Int, z: Int) -> Unit) {
    for (x in minX..maxX) {
      for (y in minY..maxY) {
        for (z in minZ..maxZ) {
          action(x, y, z)
        }
      }
    }
  }

  fun contains(x: Int, y: Int, z: Int): Boolean = x in minX..maxX && y in minY..maxY && z in minZ..maxZ

  fun contains(v: Vector): Boolean = contains(v.blockX, v.blockY, v.blockZ)
}
