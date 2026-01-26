package net.chikina.minecraft.dungeon.util

import org.bukkit.Location
import org.bukkit.util.Vector

object VectorUtils {
  fun getDirection(from: Location, to: Location): Vector {
    return to.toVector().subtract(from.toVector()).normalize()
  }

  fun moveTowards(from: Location, to: Location, distance: Double): Location {
    val direction = getDirection(from, to)
    return from.clone().add(direction.multiply(distance))
  }

  fun getLocationsAlongLine(start: Location, end: Location, step: Double): List<Location> {
    val locations = mutableListOf<Location>()
    val distance = start.distance(end)
    val direction = getDirection(start, end)

    var current = 0.0
    while (current < distance) {
      locations.add(start.clone().add(direction.clone().multiply(current)))
      current += step
    }
    return locations
  }
}
