package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.tool.GrapplingHook
import net.chikina.minecraft.dungeon.util.Messenger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GetHookCommand : CommandExecutor {
  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>,
  ): Boolean {
    if (sender !is Player) {
      Messenger.error(sender, "Only players can use this command.")
      return true
    }

    val hook = GrapplingHook()
    sender.inventory.addItem(hook.itemStack)
    Messenger.success(sender, "Given Grappling Hook!")
    return true
  }
}
