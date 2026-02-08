package net.chikina.minecraft.dungeon.map.floating.cave

import org.bukkit.util.Vector

data class CaveNode(
  val id: Int,
  val center: Vector,
  val type: CaveNodeType,
  val floorType: CaveFloorType,
) {
  val connections = mutableListOf<CaveEdge>()

  // Helper for ellipsoid bounds
  val radiusX = floorType.width / 2.0
  val radiusY = floorType.height / 2.0
  val radiusZ = floorType.length / 2.0
}
