package net.chikina.minecraft.dungeon.map.floating.cave

import org.bukkit.util.Vector

data class CaveEdge(
  val from: CaveNode,
  val to: CaveNode,
  val controlPoints: List<Vector>,
) {
  // Determine the gradient/walkability
  val isWalkable: Boolean by lazy {
    // Simple check: y difference vs horizontal distance
    val dy = to.center.y - from.center.y
    val dx = to.center.x - from.center.x
    val dz = to.center.z - from.center.z
    val distH = Math.sqrt(dx * dx + dz * dz)

    // Slope check (e.g., max 45 degrees approx 1.0)
    Math.abs(dy / distH) < 1.0
  }
}
