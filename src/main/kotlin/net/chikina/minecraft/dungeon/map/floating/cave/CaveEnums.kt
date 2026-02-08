package net.chikina.minecraft.dungeon.map.floating.cave

enum class CaveFloorType(
  val width: Double,
  val height: Double,
  val length: Double,
) {
  ENTRANCE(14.0, 8.0, 14.0),
  DEEPEST(25.0, 15.0, 25.0),
  LARGE(25.0, 12.0, 25.0), // Was 15. Increased for "Larger Rooms"
  NORMAL(18.0, 9.0, 18.0), // Was 10. Increased
  SMALL(10.0, 6.0, 10.0), // Was 6
  CORRIDOR(6.0, 5.0, 6.0),
}

enum class CaveNodeType {
  ENTRANCE,
  DEEPEST,
  MAIN_PATH,
  BRANCH,
  DEAD_END,
}
