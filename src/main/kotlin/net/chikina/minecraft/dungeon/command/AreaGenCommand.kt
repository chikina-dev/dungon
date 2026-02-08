package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.map.MapConfig
import net.chikina.minecraft.dungeon.map.MapConstants
import net.chikina.minecraft.dungeon.map.WorldMap
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.min

class AreaGenCommand : CommandExecutor {
  private val worldMap = WorldMap(MapConfig(seed = System.currentTimeMillis()))

  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>,
  ): Boolean {
    if (args.size != 6) {
      sender.sendMessage("Usage: /area_gen <x1> <y1> <z1> <x2> <y2> <z2>")
      return true
    }

    val player = sender as? Player
    if (player == null) {
      sender.sendMessage("This command can only be used by players.")
      return true
    }
    val world = player.world

    try {
      val x1 = parseCoordinate(args[0], player.location.x)
      val y1 = parseCoordinate(args[1], player.location.y)
      val z1 = parseCoordinate(args[2], player.location.z)
      val x2 = parseCoordinate(args[3], player.location.x)
      val y2 = parseCoordinate(args[4], player.location.y)
      val z2 = parseCoordinate(args[5], player.location.z)

      val scale = if (args.size > 6) args[6].toDouble() else 1.0

      val minX = minOf(x1, x2)
      val maxX = maxOf(x1, x2)
      val minY = minOf(y1, y2)
      val maxY = maxOf(y1, y2)
      val minZ = minOf(z1, z2)
      val maxZ = maxOf(z1, z2)

      val baseHeight = (minY + maxY) / 2

      val rangeHeight = maxY - minY
      val effectiveAmplitude = min(60.0, rangeHeight * 0.45)
      val seaLevelOffset = (effectiveAmplitude * 0.15).toInt()

      val seaLevel = baseHeight - seaLevelOffset

      val chunkBuffer = 20
      val chunkTop = maxY + chunkBuffer
      val maxTerrainY = maxY + 1

      val currentWorldMap =
        WorldMap(
          MapConfig(
            seed = System.currentTimeMillis(),
            scaleMultiplier = scale,
            minY = minY,
            top = chunkTop,
            baseHeight = baseHeight,
            seaLevel = seaLevel,
            maxTerrainY = maxTerrainY,
          ),
        )

      sender.sendMessage(
        "Generating area from ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ) with seed (random) and scale $scale...",
      )

      for (x in minX..maxX) {
        for (y in minY..(maxY + 3)) {
          for (z in minZ..maxZ) {
            world.getBlockAt(x, y, z).type = Material.AIR
          }
        }
      }

      val minChunkX = Math.floorDiv(minX, MapConstants.CHUNK_WIDTH)
      val maxChunkX = Math.floorDiv(maxX, MapConstants.CHUNK_WIDTH)
      val minChunkZ = Math.floorDiv(minZ, MapConstants.CHUNK_HEIGHT)
      val maxChunkZ = Math.floorDiv(maxZ, MapConstants.CHUNK_HEIGHT)

      for (cx in minChunkX..maxChunkX) {
        for (cz in minChunkZ..maxChunkZ) {
          val chunk = currentWorldMap.getChunk(cx, cz)

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

                world.getBlockAt(worldX, y, worldZ).type = material
              }
            }
          }
        }
      }
      sender.sendMessage("Generation complete.")
    } catch (e: NumberFormatException) {
      sender.sendMessage("Invalid coordinates.")
    }
    return true
  }

  private fun parseCoordinate(arg: String, current: Double): Int = if (arg.startsWith("~")) {
    val value = if (arg.length > 1) arg.substring(1).toDouble() else 0.0
    (current + value).toInt()
  } else {
    arg.toInt()
  }
}
