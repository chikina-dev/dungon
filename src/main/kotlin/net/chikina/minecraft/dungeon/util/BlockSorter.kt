package net.chikina.minecraft.dungeon.util

import org.bukkit.Location
import java.util.Comparator

object BlockSorter {
  /**
   * Sorts packed coordinates from bottom up (Y-level ascending). If Y-levels are equal, sorts by
   * distance from [center] (Inside-out).
   */
  fun sortBottomUp(packedBlocks: MutableList<Long>, center: Location) {
    packedBlocks.sortWith(
      Comparator { a, b ->
        val yA = a.packedY
        val yB = b.packedY

        if (yA != yB) {
          yA - yB
        } else {
          compareDistance(a, b, center)
        }
      },
    )
  }

  /** Sorts packed coordinates by distance from [center] (Inside-out). */
  fun sortInsideOut(packedBlocks: MutableList<Long>, center: Location) {
    packedBlocks.sortWith(Comparator { a, b -> compareDistance(a, b, center) })
  }

  private fun compareDistance(a: Long, b: Long, center: Location): Int {
    val xA = a.packedX
    val zA = a.packedZ
    val xB = b.packedX
    val zB = b.packedZ

    val distA = (xA - center.x) * (xA - center.x) + (zA - center.z) * (zA - center.z)
    val distB = (xB - center.x) * (xB - center.x) + (zB - center.z) * (zB - center.z)

    return distA.compareTo(distB)
  }
}
