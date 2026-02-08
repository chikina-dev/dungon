package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.util.Messenger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class KillEnemiesCommand : CommandExecutor {
  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>,
  ): Boolean {
    if (!sender.hasPermission("dungeon.admin")) {
      Messenger.error(sender, "You do not have permission to use this command.")
      return true
    }

    Dungeon.instance.enemySpawner.despawnAllEnemies()
    Messenger.success(sender, "All active dungeon enemies have been despawned.")
    return true
  }
}
