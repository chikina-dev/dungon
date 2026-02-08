package net.chikina.minecraft.dungeon.foraging

import net.chikina.minecraft.dungeon.map.structure.DungeonStructure
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

/** Represents a live tree instance in the dungeon. */
class TreeStructure(
  val id: UUID,
  val type: TreeType,
  val logBlocks: MutableSet<Long> = HashSet(),
) {
  val leafBlocks: MutableSet<Long> = HashSet()
  val brokenBlocks: MutableSet<Long> = HashSet()
  var worldId: UUID? = null
  var destroyedAt: Long? = null

  // Hologram references (Runtime only)
  var displayId: UUID? = null
  var center: Location? = null
  var activeTask: BukkitTask? = null

  // HP is remaining blocks
  val currentHp: Int
    get() = logBlocks.size - brokenBlocks.size

  val maxHp: Int
    get() = logBlocks.size

  fun damage(packedPos: Long) {
    if (brokenBlocks.add(packedPos)) {
      // Block successfully marked as broken
    }
  }

  fun isBroken(): Boolean = currentHp <= 0

  /** Blueprint for generating trees. Used by StructureGenerator to identify tree structures. */
  class Blueprint(
    val type: TreeType,
    width: Int,
    height: Int,
    depth: Int,
    palette: List<Material>,
    blocks: IntArray,
    rules: DungeonStructure.PlacementRules,
  ) : DungeonStructure(width, height, depth, palette, blocks, rules)

  companion object {
    fun createBlueprint(type: TreeType): Blueprint {
      val width = 5
      val height = 7
      val depth = 5

      val palette = listOf(Material.AIR, type.log, type.leaves)
      val blocks = IntArray(width * height * depth)

      fun set(x: Int, y: Int, z: Int, id: Int) {
        if (x in 0 until width && y in 0 until height && z in 0 until depth) {
          blocks[(y * depth + z) * width + x] = id
        }
      }

      val centerX = width / 2
      val centerZ = depth / 2

      // Trunk
      for (y in 0 until height - 2) {
        set(centerX, y, centerZ, 1)
      }

      // Leaves
      val leafStart = height - 4
      for (y in leafStart until height) {
        val range = if (y < height - 1) 2 else 1

        for (x in centerX - range..centerX + range) {
          for (z in centerZ - range..centerZ + range) {
            // round corners
            if (y < height - 1 &&
              (x == centerX - range || x == centerX + range) &&
              (z == centerZ - range || z == centerZ + range)
            ) {
              continue
            }

            // Don't overwrite trunk
            if (x == centerX && z == centerZ && y < height - 2) continue

            set(x, y, z, 2)
          }
        }
      }

      val rules =
        DungeonStructure.PlacementRules(
          conditions =
            listOf(
              DungeonStructure.MinElevation(0.05),
              DungeonStructure.MinHeightAboveSeaLevel(1),
            ),
          spawnChance = 0.9,
          zIndex = 5,
        )

      return Blueprint(type, width, height, depth, palette, blocks, rules)
    }
  }
}
