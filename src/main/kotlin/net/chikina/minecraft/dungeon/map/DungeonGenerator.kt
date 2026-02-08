package net.chikina.minecraft.dungeon.map

import org.bukkit.Material
import org.bukkit.World
import kotlin.math.min

object DungeonGenerator {
  fun generate(dungeon: ExplorationDungeon, world: World) {
    val minX = minOf(dungeon.x1, dungeon.x2)
    val maxX = maxOf(dungeon.x1, dungeon.x2)
    val minY = minOf(dungeon.y1, dungeon.y2)
    val maxY = maxOf(dungeon.y1, dungeon.y2)
    val minZ = minOf(dungeon.z1, dungeon.z2)
    val maxZ = maxOf(dungeon.z1, dungeon.z2)

    // These calculations should match AreaGenCommand logic or be configurable
    val baseHeight = (minY + maxY) / 2
    val rangeHeight = maxY - minY
    val effectiveAmplitude = min(60.0, rangeHeight * 0.45)
    val seaLevelOffset = (effectiveAmplitude * 0.15).toInt()
    val seaLevel = baseHeight - seaLevelOffset

    val chunkBuffer = 20
    val chunkTop = maxY + chunkBuffer
    val maxTerrainY = maxY + 1

    val config =
      MapConfig(
        seed = dungeon.seed,
        scaleMultiplier = 1.0, // Default
        minY = minY,
        top = chunkTop,
        baseHeight = baseHeight,
        seaLevel = seaLevel,
        maxTerrainY = maxTerrainY,
      )

    val generator = WorldMap(config)

    // 1. Clear Area (Optional: might be heavy, but necessary to remove old structures)
    // AreaGenCommand clears up to maxY + 3
    for (x in minX..maxX) {
      for (y in minY..(maxY + 3)) {
        for (z in minZ..maxZ) {
          if (world.getBlockAt(x, y, z).type != Material.AIR) {
            world.getBlockAt(x, y, z).type = Material.AIR
          }
        }
      }
    }

    // 2. Generate and Paste
    val minChunkX = Math.floorDiv(minX, MapConstants.CHUNK_WIDTH)
    val maxChunkX = Math.floorDiv(maxX, MapConstants.CHUNK_WIDTH)
    val minChunkZ = Math.floorDiv(minZ, MapConstants.CHUNK_HEIGHT)
    val maxChunkZ = Math.floorDiv(maxZ, MapConstants.CHUNK_HEIGHT)

    for (cx in minChunkX..maxChunkX) {
      for (cz in minChunkZ..maxChunkZ) {
        val chunk = generator.getChunk(cx, cz)

        for (lx in 0 until MapConstants.CHUNK_WIDTH) {
          for (lz in 0 until MapConstants.CHUNK_HEIGHT) {
            val worldX = cx * MapConstants.CHUNK_WIDTH + lx
            val worldZ = cz * MapConstants.CHUNK_HEIGHT + lz

            if (worldX !in minX..maxX || worldZ !in minZ..maxZ) continue

            for (y in chunk.minY until chunk.top) {
              val shouldPaste =
                if (y <= maxY) {
                  true
                } else {
                  chunk.getBlock(lx, lz, y) != 0
                }

              if (!shouldPaste) continue

              val blockId = chunk.getBlock(lx, lz, y)
              val material = Material.values().getOrNull(blockId) ?: Material.AIR

              if (world.getBlockAt(worldX, y, worldZ).type != material) {
                world.getBlockAt(worldX, y, worldZ).type = material
              }
            }
          }
        }
      }
    }
  }
}
