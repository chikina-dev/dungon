package net.chikina.minecraft.dungeon.foraging

import net.chikina.minecraft.dungeon.util.BlockSorter
import net.chikina.minecraft.dungeon.util.BlockTaskScheduler
import net.chikina.minecraft.dungeon.util.CoordinatePacker
import net.chikina.minecraft.dungeon.util.packedX
import net.chikina.minecraft.dungeon.util.packedY
import net.chikina.minecraft.dungeon.util.packedZ
import net.chikina.minecraft.dungeon.util.toLocation
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TreeManager {
  private val blockMap = ConcurrentHashMap<Long, UUID>()

  private val activeTrees = ConcurrentHashMap<UUID, TreeStructure>()

  fun registerTree(tree: TreeStructure) {
    val existing = activeTrees.putIfAbsent(tree.id, tree)
    if (existing == null) {
      for (packedPos in tree.logBlocks) {
        blockMap[packedPos] = tree.id
      }
      for (packedPos in tree.leafBlocks) {
        blockMap[packedPos] = tree.id
      }
      // DEBUG LOG
      Bukkit
        .getLogger()
        .info(
          "Registered Tree ${tree.id} with ${tree.logBlocks.size} logs and ${tree.leafBlocks.size} leaves.",
        )
    }
  }

  fun getOrCreateTree(id: UUID, factory: () -> TreeStructure): TreeStructure = activeTrees.computeIfAbsent(id) { factory() }

  fun addLogBlock(treeId: UUID, packedPos: Long) {
    val tree = activeTrees[treeId] ?: return
    if (tree.logBlocks.add(packedPos)) {
      blockMap[packedPos] = treeId
    }
  }

  fun addLeafBlock(treeId: UUID, packedPos: Long) {
    val tree = activeTrees[treeId] ?: return
    if (tree.leafBlocks.add(packedPos)) {
      blockMap[packedPos] = treeId
    }
  }

  fun isTrackedLeaf(block: Block): Boolean {
    val packed = CoordinatePacker.pack(block.x, block.y, block.z)
    val treeId = blockMap[packed] ?: return false
    val tree = activeTrees[treeId] ?: return false
    return tree.leafBlocks.contains(packed)
  }

  fun handleBlockBreak(block: Block): Boolean {
    if (!block.type.name.endsWith("_LOG")) return false

    val packedPos = CoordinatePacker.pack(block.x, block.y, block.z)
    val treeId = blockMap[packedPos]
    if (treeId == null) {
      Bukkit
        .getLogger()
        .warning(
          "TreeManager: Block at ${block.x}, ${block.y}, ${block.z} (Packed: $packedPos) is LOG but not found in blockMap.",
        )
      return false
    }
    val tree = activeTrees[treeId] ?: return false

    if (tree.worldId == null) {
      tree.worldId = block.world.uid
    }

    tree.damage(packedPos)
    tree.destroyedAt = System.currentTimeMillis()

    if (tree.isBroken()) {
      // Halve the regen time (effectively setting destroyedAt further back, or handling in tick)
      // For now, let's just mark it.
      // In a real implementation, we might want to physically remove the rest of the tree or play
      // effects.
    }

    return true
  }

  fun getTree(id: UUID): TreeStructure? = activeTrees[id]

  fun getTreeId(block: Block): UUID? = blockMap[
    net.chikina.minecraft.dungeon.util.CoordinatePacker
      .pack(block.x, block.y, block.z),
  ]

  fun reset() {
    blockMap.clear()
    activeTrees.clear()
  }

  fun tick() {
    val now = System.currentTimeMillis()
    val baseRegenTime = 60000L // 60 seconds
    val brokenRegenTime = 30000L // 30 seconds (halved)

    for (tree in activeTrees.values) {
      updateTreeState(tree, now, baseRegenTime, brokenRegenTime)
    }
  }

  private fun updateTreeState(
    tree: TreeStructure,
    now: Long,
    baseRegenTime: Long,
    brokenRegenTime: Long,
  ) {
    val lastDamage = tree.destroyedAt ?: return
    val requiredTime = if (tree.isBroken()) brokenRegenTime else baseRegenTime
    val remainingMillis = requiredTime - (now - lastDamage)

    // Ensure center is calculated for visualizer
    if (tree.center == null && tree.worldId != null) {
      val world = Bukkit.getWorld(tree.worldId!!)
      if (world != null) calculateCenter(tree, world)
    }

    TreeVisualizer.updateHologram(tree, remainingMillis)

    if (remainingMillis <= 0) {
      regenerateTree(tree)
    } else if (tree.isBroken() && tree.activeTask == null) {
      destroyLeaves(tree)
    }
  }

  private fun regenerateTree(tree: TreeStructure) {
    tree.destroyedAt = null
    TreeVisualizer.removeHologram(tree)

    // Cancel any existing task
    tree.activeTask?.cancel()
    tree.activeTask = null

    val worldId = tree.worldId ?: return
    val world = Bukkit.getWorld(worldId) ?: return

    // 1. Collect Logs
    val toRestore = ArrayList<Pair<Long, Material>>()
    for (packed in tree.brokenBlocks) {
      toRestore.add(packed to tree.type.log)
    }
    tree.brokenBlocks.clear()

    // 2. Collect Leaves
    for (packed in tree.leafBlocks) {
      val loc = packed.toLocation(world)
      if (loc.block.type != tree.type.leaves) {
        toRestore.add(packed to tree.type.leaves)
      }
    }

    if (toRestore.isEmpty()) return

    // 3. Sort
    val center = calculateCenter(tree, world) ?: return
    BlockSorter.sortBottomUp(toRestore.map { it.first }.toMutableList(), center)
    toRestore.sortWith(
      Comparator { a, b ->
        val yA = a.first.packedY
        val yB = b.first.packedY
        if (yA != yB) yA - yB else compareDistance(a.first, b.first, center)
      },
    )

    // 4. Schedule
    tree.activeTask =
      BlockTaskScheduler.run(toRestore, perTick = 20) { (packed, material) ->
        val loc = packed.toLocation(world)
        loc.block.type = material
        world.playEffect(loc, Effect.STEP_SOUND, material)
      }
  }

  private fun destroyLeaves(tree: TreeStructure) {
    val worldId = tree.worldId ?: return
    val world = Bukkit.getWorld(worldId) ?: return

    // 1. Collect
    val toBreak = ArrayList<Long>()
    for (packed in tree.leafBlocks) {
      val loc = packed.toLocation(world)
      if (loc.block.type != Material.AIR) {
        toBreak.add(packed)
      }
    }

    if (toBreak.isEmpty()) return

    // 2. Sort
    val center = calculateCenter(tree, world) ?: return
    BlockSorter.sortInsideOut(toBreak, center)

    // 3. Schedule
    tree.activeTask =
      BlockTaskScheduler.run(toBreak, perTick = 20) { packed ->
        val loc = packed.toLocation(world)
        if (loc.block.type != Material.AIR) {
          world.playEffect(loc, Effect.STEP_SOUND, loc.block.type)
          loc.block.type = Material.AIR
        }
      }
  }

  private fun calculateCenter(tree: TreeStructure, world: org.bukkit.World): Location? {
    if (tree.center != null) return tree.center
    if (tree.logBlocks.isEmpty()) return null

    var sumX = 0.0
    var sumY = 0.0
    var sumZ = 0.0
    for (packed in tree.logBlocks) {
      sumX += packed.packedX
      sumY += packed.packedY
      sumZ += packed.packedZ
    }
    val count = tree.logBlocks.size
    tree.center = Location(world, sumX / count + 0.5, sumY / count + 1.5, sumZ / count + 0.5)
    return tree.center
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
