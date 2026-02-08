package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.map.floating.FloatingIslandGenerator
import net.chikina.minecraft.dungeon.map.floating.config.FloatingIslandConfig
import net.chikina.minecraft.dungeon.map.floating.config.IslandBounds
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FloatingIslandGenCommand : CommandExecutor {
  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>,
  ): Boolean {
    if (sender !is Player) {
      sender.sendMessage("This command is for players only.")
      return true
    }

    if (args.size < 6) {
      sender.sendMessage(
        "Usage: /island_gen <x1> <y1> <z1> <x2> <y2> <z2> [horizonRatio] [caveScale]",
      )
      return true
    }

    try {
      val x1 = parseCoordinate(args[0], sender.location.x)
      val y1 = parseCoordinate(args[1], sender.location.y)
      val z1 = parseCoordinate(args[2], sender.location.z)
      val x2 = parseCoordinate(args[3], sender.location.x)
      val y2 = parseCoordinate(args[4], sender.location.y)
      val z2 = parseCoordinate(args[5], sender.location.z)

      val horizonRatio = if (args.size > 6) args[6].toDouble() else 0.8
      val caveScale = if (args.size > 7) args[7].toDouble() else 1.0

      val bounds = IslandBounds(x1, y1, z1, x2, y2, z2)
      val config =
        FloatingIslandConfig(
          seed = System.currentTimeMillis(),
          bounds = bounds,
          horizonRatio = horizonRatio,
          caveScale = caveScale,
        )

      sender.sendMessage("Generating Floating Island...")
      sender.sendMessage("Bounds: $bounds")
      sender.sendMessage("Horizon Ratio: $horizonRatio, Cave Scale: $caveScale")

      // Run async to avoid lagging main thread too much (though Bukkit API editing must be sync)
      // For this simple command, we'll run sync but normally we'd queue blocks.
      // Assuming the generators use Bukkit API directly, must run sync.

      val generator = FloatingIslandGenerator(config)
      generator.generate(sender.world)

      sender.sendMessage("Generation complete.")
    } catch (e: Exception) {
      sender.sendMessage("Error: ${e.message}")
      e.printStackTrace()
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
