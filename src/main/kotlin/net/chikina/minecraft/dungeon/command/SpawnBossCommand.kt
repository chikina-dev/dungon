package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.enemy.impl.StarfallCleric
import net.chikina.minecraft.dungeon.util.Messenger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpawnBossCommand : CommandExecutor {
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

        // 後でboss関連のコマンドを実装する

        val boss = StarfallCleric()
        boss.spawn(sender.location.clone())

        boss.getLivingEntity()?.uniqueId?.let { uuid ->
            Dungeon.instance.enemySpawner.registerOneTimeEnemy(boss)
        }

        Messenger.success(sender, "Spawned Starfall Cleric!")
        return true
    }
}
