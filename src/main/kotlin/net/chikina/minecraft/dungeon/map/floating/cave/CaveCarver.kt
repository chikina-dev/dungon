package net.chikina.minecraft.dungeon.map.floating.cave

import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import net.chikina.minecraft.dungeon.math.noise.Perlin
import net.chikina.minecraft.dungeon.util.Region3D
import org.bukkit.Material
import org.bukkit.util.Vector

class CaveCarver(
  private val config: FloatingIslandConfig,
) {
  // Simple Perlin noise for roughness
  private val noiseGen = Perlin(config.seed)

  fun carving(buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer, graph: CaveGraph) {
    for (node in graph.nodes) {
      carveRoom(buffer, node)
    }

    for (edge in graph.edges) {
      carveTunnel(buffer, edge, graph)
    }
  }

  private fun carveRoom(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    node: CaveNode,
  ) {
    val center = node.center
    val rx = node.radiusX * config.caveScale
    val ry = node.radiusY * config.caveScale
    val rz = node.radiusZ * config.caveScale

    val region =
      Region3D(
        (center.x - rx - 2).toInt(),
        (center.y - ry - 2).toInt(),
        (center.z - rz - 2).toInt(),
        (center.x + rx + 2).toInt(),
        (center.y + ry + 2).toInt(),
        (center.z + rz + 2).toInt(),
      )

    region.forEach { x: Int, y: Int, z: Int ->
      val noiseAmt = noiseGen.noise(x * 0.15, y * 0.15, z * 0.15) * 0.2

      val dx = (x - center.x) / (rx * (1.0 + noiseAmt))
      val dy = (y - center.y) / (ry * (1.0 + noiseAmt))
      val dz = (z - center.z) / (rz * (1.0 + noiseAmt))

      val distSq = dx * dx + dy * dy + dz * dz

      if (distSq <= 1.0) {
        if (y > config.bounds.minY) {
          buffer.setBlock(x, y, z, Material.AIR)
        }
      }
    }
  }

  private fun carveTunnel(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    edge: CaveEdge,
    graph: CaveGraph,
  ) {
    val p0 = edge.from.center
    val p3 = edge.to.center

    val points =
      if (edge.controlPoints.isNotEmpty()) {
        val p1 = edge.controlPoints[0]
        if (edge.controlPoints.size >= 2) {
          generateCubicBezierPoints(p0, edge.controlPoints[0], edge.controlPoints[1], p3, 20)
        } else {
          generateBezierPoints(p0, p1, p3, 20)
        }
      } else {
        generateLinearPoints(p0, p3, 20)
      }

    val radius = 3.0 * config.caveScale

    for (point in points) {
      if (isInAnyRoom(point, graph)) continue

      carveSphere(buffer, point, radius)
    }
  }

  private fun isInAnyRoom(point: Vector, graph: CaveGraph): Boolean {
    for (node in graph.nodes) {
      if (node.floorType == CaveFloorType.CORRIDOR) continue // Don't respect corridors as "Rooms"

      val rx = node.radiusX * config.caveScale
      val ry = node.radiusY * config.caveScale
      val rz = node.radiusZ * config.caveScale

      if (Math.abs(point.x - node.center.x) > rx) continue
      if (Math.abs(point.y - node.center.y) > ry) continue
      if (Math.abs(point.z - node.center.z) > rz) continue

      val dx = (point.x - node.center.x) / rx
      val dy = (point.y - node.center.y) / ry
      val dz = (point.z - node.center.z) / rz

      if (dx * dx + dy * dy + dz * dz < 0.8) {
        return true
      }
    }
    return false
  }

  private fun generateCubicBezierPoints(
    p0: Vector,
    p1: Vector,
    p2: Vector,
    p3: Vector,
    steps: Int,
  ): List<Vector> {
    val list = mutableListOf<Vector>()
    for (i in 0..steps) {
      val t = i.toDouble() / steps
      val u = 1 - t
      val tt = t * t
      val uu = u * u
      val ttt = tt * t
      val uuu = uu * u

      val p =
        p0
          .clone()
          .multiply(uuu)
          .add(p1.clone().multiply(3 * uu * t))
          .add(p2.clone().multiply(3 * u * tt))
          .add(p3.clone().multiply(ttt))
      list.add(p)
    }
    return list
  }

  private fun generateBezierPoints(p0: Vector, p1: Vector, p2: Vector, steps: Int): List<Vector> {
    val list = mutableListOf<Vector>()
    for (i in 0..steps) {
      val t = i.toDouble() / steps
      val u = 1 - t
      val tt = t * t
      val uu = u * u

      val p =
        p0
          .clone()
          .multiply(uu)
          .add(p1.clone().multiply(2 * u * t))
          .add(p2.clone().multiply(tt))
      list.add(p)
    }
    return list
  }

  private fun generateLinearPoints(p0: Vector, p1: Vector, steps: Int): List<Vector> {
    val list = mutableListOf<Vector>()
    val dir = p1.clone().subtract(p0).multiply(1.0 / steps)
    for (i in 0..steps) {
      list.add(p0.clone().add(dir.clone().multiply(i.toDouble())))
    }
    return list
  }

  private fun carveSphere(
    buffer: net.chikina.minecraft.dungeon.util.AsyncBlockBuffer,
    center: Vector,
    radius: Double,
  ) {
    val r = radius.toInt() + 1

    val cx = center.blockX
    val cy = center.blockY
    val cz = center.blockZ

    val region = Region3D(cx - r, cy - r, cz - r, cx + r, cy + r, cz + r)

    region.forEach { x: Int, y: Int, z: Int ->
      val dx: Double = x.toDouble() - center.x
      val dy: Double = y.toDouble() - center.y
      val dz: Double = z.toDouble() - center.z
      val distSq: Double = dx * dx + dy * dy + dz * dz

      val maxRadius: Double = (radius + 1.5)
      if (distSq <= maxRadius * maxRadius) {
        val noise = noiseGen.noise(x * 0.2, y * 0.2, z * 0.2) * 1.5
        val noisyRadius: Double = radius + noise
        val noisyRadiusSq: Double = noisyRadius * noisyRadius

        if (distSq <= noisyRadiusSq) {
          // Optimized: blindly set valid locations to AIR.
          // AsyncBlockBuffer handles overwrites efficiently.
          // Note: In original code we checked if block was not AIR.
          // Here we just write AIR. It's safe because usually AIR over AIR is fine in map.
          // However, if we want to save memory in the buffer we might want to check?
          // Since it's async, we can check the buffer? No, buffer might be empty.
          // Just writing AIR is safest and fastest for "carving".
          buffer.setBlock(x, y, z, Material.AIR)
        }
      }
    }
  }
}
