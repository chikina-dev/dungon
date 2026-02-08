package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.map.ExplorationDungeonManager
import net.chikina.minecraft.dungeon.util.Messenger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RegenDungeonCommand(
  private val dungeonManager: ExplorationDungeonManager,
) : CommandExecutor {
  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>,
  ): Boolean {
    if (args.isEmpty()) {
      Messenger.error(sender, "Usage: /regendungeon <name>")
      return true
    }

    val name = args[0]
    val dungeon = Dungeon.instance.explorationDungeonRepository.findByName(name)

    if (dungeon == null) {
      Messenger.error(sender, "Dungeon not found: $name")
      return true
    }

    dungeonManager.regenerate(dungeon)
    Messenger.success(sender, "Regenerated dungeon: $name")
    return true
  }
}
