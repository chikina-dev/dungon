package net.chikina.minecraft.dungeon.map.floating.cave

class CaveGraph {
  val nodes = mutableListOf<CaveNode>()
  val edges = mutableListOf<CaveEdge>()

  lateinit var entranceNode: CaveNode
  lateinit var deepestNode: CaveNode

  fun addNode(node: CaveNode) {
    nodes.add(node)
  }

  fun addEdge(edge: CaveEdge) {
    edges.add(edge)
    edge.from.connections.add(edge)
    edge.to.connections.add(edge)
  }
}
