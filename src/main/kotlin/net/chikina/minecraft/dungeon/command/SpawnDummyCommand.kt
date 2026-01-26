package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.util.Messenger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpawnDummyCommand : CommandExecutor {
    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        if (!sender.hasPermission("dungeon.admin")) {
            Messenger.error(sender, "You do not have permission to use this command.")
            return true
        }

        Dungeon.instance.enemySpawner.addSpawnPoint(sender.location.clone(), "DUMMY")
        Messenger.success(sender, "Spawned a Damage Dummy at your location.")
        return true
    }
}
