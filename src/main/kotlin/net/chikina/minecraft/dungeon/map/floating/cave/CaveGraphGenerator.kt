package net.chikina.minecraft.dungeon.map.floating.cave

import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import org.bukkit.util.Vector
import java.util.Random

class CaveGraphGenerator(
  private val config: FloatingIslandConfig,
) {
  private val random = Random(config.seed)

  fun generate(entranceCenter: Vector): CaveGraph {
    val graph = CaveGraph()

    val islandHeight = config.bounds.height.toDouble()
    val underMinY = config.bounds.minY.toDouble()

    // Surface Buffer
    val caveMinY = underMinY + (islandHeight * 0.15)
    val caveMaxY = underMinY + (islandHeight * 0.75)

    val centerX = (config.bounds.minX + config.bounds.maxX) / 2.0
    val centerZ = (config.bounds.minZ + config.bounds.maxZ) / 2.0

    val maxRadiusX = (config.bounds.width / 2.0)
    val maxRadiusZ = (config.bounds.depth / 2.0)
    val safeRadiusX = maxRadiusX * 0.75
    val safeRadiusZ = maxRadiusZ * 0.75

    // Entrance Node
    val entrance =
      CaveNode(0, entranceCenter.clone(), CaveNodeType.ENTRANCE, CaveFloorType.ENTRANCE)
    graph.addNode(entrance)
    graph.entranceNode = entrance

    // Grid Fill
    val gridNodes =
      generateDenseGrid(graph, centerX, centerZ, caveMinY, caveMaxY, safeRadiusX, safeRadiusZ)

    // Grid Connectivity
    connectGridMesh(graph, gridNodes)

    // Entrance Connection
    connectEntranceSmart(graph, entrance)

    // Global Connectivity
    ensureConnectivity(graph, entrance)

    // Overlap Resolution
    resolveOverlaps(graph)

    // Assign Deepest
    val deepest = graph.nodes.minByOrNull { it.center.y }
    if (deepest != null && deepest != entrance) {
      graph.deepestNode = deepest
    }

    return graph
  }

  private fun generateDenseGrid(
    graph: CaveGraph,
    centerX: Double,
    centerZ: Double,
    minY: Double,
    maxY: Double,
    safeRadiusX: Double,
    safeRadiusZ: Double,
  ): Map<Triple<Int, Int, Int>, CaveNode> {
    val gridMap = HashMap<Triple<Int, Int, Int>, CaveNode>()

    val hStep = 30.0
    val vStep = 18.0

    val startX = config.bounds.minX.toDouble()
    val endX = config.bounds.maxX.toDouble()
    val startZ = config.bounds.minZ.toDouble()
    val endZ = config.bounds.maxZ.toDouble()

    val islandHeight = config.bounds.height.toDouble()
    val centerY = config.bounds.minY + (islandHeight / 2.0)
    val safeRadiusY = (islandHeight / 2.0) * 0.75

    val ellipsoid =
      net.chikina.minecraft.dungeon.util.Ellipsoid(
        Vector(centerX, centerY, centerZ),
        safeRadiusX,
        safeRadiusY,
        safeRadiusZ,
      )

    var ix = 0
    var x = startX
    while (x <= endX) {
      var iz = 0
      var z = startZ
      while (z <= endZ) {
        var iy = 0
        var y = minY
        while (y <= maxY) {
          val node = tryPlaceNode(graph, x, y, z, ellipsoid, hStep, vStep)

          if (node != null) {
            gridMap[Triple(ix, iy, iz)] = node
          }

          y += vStep
          iy++
        }
        z += hStep
        iz++
      }
      x += hStep
      ix++
    }
    return gridMap
  }

  private fun connectGridMesh(graph: CaveGraph, gridNodes: Map<Triple<Int, Int, Int>, CaveNode>) {
    for ((key, node) in gridNodes) {
      val (ix, iy, iz) = key

      val neighborKeys =
        listOf(
          Triple(ix + 1, iy, iz), // East
          Triple(ix, iy + 1, iz), // Up
          Triple(ix, iy, iz + 1), // South
        )

      for (nKey in neighborKeys) {
        val neighbor = gridNodes[nKey] ?: continue

        if (node.center.distance(neighbor.center) <= 60.0) {
          connectSmart(graph, node, neighbor)
        }
      }

      // Diagonal Connection
      if (random.nextDouble() < 0.2) {
        val diagKey = Triple(ix + 1, iy, iz + 1)
        val diagNeighbor = gridNodes[diagKey]
        if (diagNeighbor != null) {
          connectSmart(graph, node, diagNeighbor)
        }
      }
    }
  }

  private fun connectEntranceSmart(graph: CaveGraph, entrance: CaveNode) {
    val nearest =
      graph.nodes.filter { it != entrance }.minByOrNull {
        it.center.distance(entrance.center)
      }
        ?: return

    connectSmart(graph, entrance, nearest)
  }

  private fun connectSmart(graph: CaveGraph, from: CaveNode, to: CaveNode) {
    val dy = Math.abs(to.center.y - from.center.y)
    val dxz =
      Math.sqrt(
        Math.pow(to.center.x - from.center.x, 2.0) +
          Math.pow(to.center.z - from.center.z, 2.0),
      )

    val slope = if (dxz < 0.1) 999.0 else dy / dxz
    val maxSlope = 0.577 // tan(30)

    if (slope > maxSlope) {
      generateHelixPath(graph, from, to)
    } else {
      generateOrganicPath(graph, from, to)
    }
  }

  private fun generateHelixPath(graph: CaveGraph, from: CaveNode, to: CaveNode) {
    val dy = to.center.y - from.center.y
    val steps = Math.ceil(Math.abs(dy) / 4.0).toInt().coerceAtLeast(5)

    val diff = to.center.clone().subtract(from.center)
    val groundDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z)
    val radius = Math.max(10.0, groundDist * 0.5)
    val rotations = 1.0 + random.nextDouble()

    var prevNode = from

    for (i in 1 until steps) {
      val t = i.toDouble() / steps

      val angle = t * rotations * 2 * Math.PI

      val y = from.center.y + (dy * t)
      val turnX = Math.cos(angle) * radius
      val turnZ = Math.sin(angle) * radius

      val currentCenterX = from.center.x * (1.0 - t) + to.center.x * t
      val currentCenterZ = from.center.z * (1.0 - t) + to.center.z * t

      var pos = Vector(currentCenterX + turnX, y, currentCenterZ + turnZ)

      pos = clampToEllipsoid(pos)

      val node = CaveNode(graph.nodes.size, pos, CaveNodeType.MAIN_PATH, CaveFloorType.CORRIDOR)
      graph.addNode(node)

      connectLinear(graph, prevNode, node)
      prevNode = node
    }
    connectLinear(graph, prevNode, to)
  }

  private fun generateOrganicPath(graph: CaveGraph, from: CaveNode, to: CaveNode) {
    val dir = to.center
      .clone()
      .subtract(from.center)
      .normalize()
    val dist = from.center.distance(to.center)

    val up = Vector(0, 1, 0)
    var right = dir.getCrossProduct(up)
    if (right.lengthSquared() < 0.001) right = Vector(1, 0, 0)
    right.normalize()

    val maxOffset = Math.min(dist * 0.6, 25.0)

    val biasUp = if (random.nextBoolean()) 1.0 else -1.0
    val biasRight = if (random.nextBoolean()) 1.0 else -1.0

    val offset1 = biasRight * (0.5 + random.nextDouble() * 0.5) * maxOffset
    val offset2 = biasRight * (0.5 + random.nextDouble() * 0.5) * maxOffset

    var cp1 =
      from.center
        .clone()
        .add(dir.clone().multiply(dist * 0.33))
        .add(right.clone().multiply(offset1))
        .add(up.clone().multiply(biasUp * maxOffset * 0.3))

    var cp2 =
      from.center
        .clone()
        .add(dir.clone().multiply(dist * 0.66))
        .add(right.clone().multiply(offset2 * if (random.nextBoolean()) -1.0 else 1.0))
        .add(up.clone().multiply(biasUp * maxOffset * 0.3))

    cp1 = clampToEllipsoid(cp1)
    cp2 = clampToEllipsoid(cp2)

    val edge = CaveEdge(from, to, listOf(cp1, cp2))
    graph.addEdge(edge)
  }

  private fun clampToEllipsoid(pos: Vector): Vector {
    val islandHeight = config.bounds.height.toDouble()
    val centerY = config.bounds.minY + (islandHeight / 2.0)
    val safeRadiusY = (islandHeight / 2.0) * 0.75

    val centerX = (config.bounds.minX + config.bounds.maxX) / 2.0
    val centerZ = (config.bounds.minZ + config.bounds.maxZ) / 2.0
    val safeRadiusX = (config.bounds.width / 2.0) * 0.75
    val safeRadiusZ = (config.bounds.depth / 2.0) * 0.75

    val ellipsoid =
      net.chikina.minecraft.dungeon.util.Ellipsoid(
        Vector(centerX, centerY, centerZ),
        safeRadiusX,
        safeRadiusY,
        safeRadiusZ,
      )

    val tunnelRadius = 3.0 * config.caveScale + 2.0

    if (ellipsoid.contains(pos.x, pos.y, pos.z, tunnelRadius)) {
      return pos
    }

    val dx = (pos.x - centerX)
    val dy = (pos.y - centerY)
    val dz = (pos.z - centerZ)

    val nx = dx / (safeRadiusX - tunnelRadius)
    val ny = dy / (safeRadiusY - tunnelRadius)
    val nz = dz / (safeRadiusZ - tunnelRadius)

    val len = Math.sqrt(nx * nx + ny * ny + nz * nz)
    if (len < 0.0001) return pos

    val factor = 1.0 / len

    val newDx = dx * factor
    val newDy = dy * factor
    val newDz = dz * factor

    return Vector(centerX + newDx, centerY + newDy, centerZ + newDz)
  }

  private fun tryPlaceNode(
    graph: CaveGraph,
    x: Double,
    y: Double,
    z: Double,
    ellipsoid: net.chikina.minecraft.dungeon.util.Ellipsoid,
    hStep: Double,
    vStep: Double,
  ): CaveNode? {
    val type = if (random.nextDouble() < 0.2) CaveNodeType.MAIN_PATH else CaveNodeType.BRANCH
    val floor = if (type == CaveNodeType.MAIN_PATH) CaveFloorType.LARGE else CaveFloorType.NORMAL
    val nodeRadius = Math.max(floor.width, floor.length) / 2.0

    if (!ellipsoid.contains(x, y, z, nodeRadius + 1.0)) return null

    val jitterX = (random.nextDouble() - 0.5) * (hStep * 0.25)
    val jitterY = (random.nextDouble() - 0.5) * (vStep * 0.25)
    val jitterZ = (random.nextDouble() - 0.5) * (hStep * 0.25)
    val pos = Vector(x + jitterX, y + jitterY, z + jitterZ)

    if (!ellipsoid.contains(pos.x, pos.y, pos.z, nodeRadius + 0.5)) return null

    if (pos.y < config.bounds.minY || pos.y > config.bounds.maxY) return null

    if (isOverlapping(graph, pos, nodeRadius)) return null

    val node = CaveNode(graph.nodes.size, pos, type, floor)
    graph.addNode(node)
    return node
  }

  private fun isOverlapping(graph: CaveGraph, pos: Vector, radius: Double): Boolean {
    val buffer = 5.0
    for (node in graph.nodes) {
      val dist = node.center.distance(pos)
      val otherRadius = Math.max(node.floorType.width, node.floorType.length) / 2.0
      if (dist < (radius + otherRadius + buffer)) {
        return true
      }
    }
    return false
  }

  private fun connectLinear(graph: CaveGraph, from: CaveNode, to: CaveNode) {
    // Just a straight edge, but used for small segments of a helix
    graph.addEdge(CaveEdge(from, to, listOf()))
  }

  private fun ensureConnectivity(graph: CaveGraph, startNode: CaveNode) {
    // Standard connectivity check
    while (true) {
      val visited = HashSet<CaveNode>()
      val q = java.util.LinkedList<CaveNode>()
      q.add(startNode)
      visited.add(startNode)
      while (!q.isEmpty()) {
        val curr = q.poll()
        for (conn in curr.connections) {
          val other = if (conn.from == curr) conn.to else conn.from
          if (visited.add(other)) q.add(other)
        }
      }
      if (visited.size == graph.nodes.size) break

      val unconnected = graph.nodes.filter { !visited.contains(it) }
      var bestA: CaveNode? = null
      var bestB: CaveNode? = null
      var minDist = Double.MAX_VALUE

      for (u in unconnected) {
        for (v in visited) {
          val dist = u.center.distance(v.center)
          if (dist < minDist) {
            minDist = dist
            bestA = v
            bestB = u
          }
        }
      }
      if (bestA != null && bestB != null) {
        connectSmart(graph, bestA, bestB)
      } else {
        break
      }
    }
  }

  private fun resolveOverlaps(graph: CaveGraph) {
    val iterations = 3
    for (i in 0 until iterations) {
      for (nodeA in graph.nodes) {
        if (nodeA.type == CaveNodeType.ENTRANCE) continue

        for (nodeB in graph.nodes) {
          if (nodeA == nodeB) continue
          val distSq = nodeA.center.distanceSquared(nodeB.center)
          val combinedRadius = 15.0
          if (distSq < combinedRadius * combinedRadius) {
            val dist = Math.sqrt(distSq)
            if (dist < 0.1) continue
            val pushVec = nodeA.center
              .clone()
              .subtract(nodeB.center)
              .normalize()
              .multiply(0.5)
            nodeA.center.add(pushVec)
          }
        }
      }
    }
  }
}
