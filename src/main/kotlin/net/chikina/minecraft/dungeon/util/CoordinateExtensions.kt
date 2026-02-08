package net.chikina.minecraft.dungeon.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import java.util.UUID

/**
 * Extension properties for working with packed coordinates (Long). Delegates to CoordinatePacker
 * for implementation.
 */
val Long.packedX: Int
  get() = CoordinatePacker.unpackX(this)

val Long.packedY: Int
  get() = CoordinatePacker.unpackY(this)

val Long.packedZ: Int
  get() = CoordinatePacker.unpackZ(this)

fun Long.toLocation(world: World): Location = Location(world, packedX.toDouble(), packedY.toDouble(), packedZ.toDouble())

fun Long.toLocation(worldId: UUID?): Location? {
  if (worldId == null) return null
  val world = Bukkit.getWorld(worldId) ?: return null
  return toLocation(world)
}
