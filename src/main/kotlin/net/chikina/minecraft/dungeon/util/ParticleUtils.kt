package net.chikina.minecraft.dungeon.util

import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.Location
import org.bukkit.Particle

object ParticleUtils {
  fun drawLine(
          start: Location,
          end: Location,
          particle: Particle,
          count: Int,
          step: Double,
          offsetX: Double = 0.0,
          offsetY: Double = 0.0,
          offsetZ: Double = 0.0,
          extra: Double = 0.0,
          data: Any? = null
  ) {
    val locations = VectorUtils.getLocationsAlongLine(start, end, step)
    for (loc in locations) {
      loc.world.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra, data)
    }
  }

  fun drawCircle(
          center: Location,
          radius: Double,
          particle: Particle,
          points: Int,
          offsetX: Double = 0.0,
          offsetY: Double = 0.0,
          offsetZ: Double = 0.0,
          extra: Double = 0.0,
          data: Any? = null
  ) {
    val world = center.world
    val increment = (2 * Math.PI) / points

    for (i in 0 until points) {
      val angle = i * increment
      val x = center.x + (radius * cos(angle))
      val z = center.z + (radius * sin(angle))
      val loc = Location(world, x, center.y, z)
      world.spawnParticle(particle, loc, 1, offsetX, offsetY, offsetZ, extra, data)
    }
  }
}
