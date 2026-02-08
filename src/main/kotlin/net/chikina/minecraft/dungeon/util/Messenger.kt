package net.chikina.minecraft.dungeon.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

object Messenger {
  private val PREFIX = Component.text("[Dungeon] ", NamedTextColor.GRAY)

  fun send(sender: CommandSender, message: String) {
    sender.sendMessage(PREFIX.append(Component.text(message)))
  }

  fun send(sender: CommandSender, message: Component) {
    sender.sendMessage(PREFIX.append(message))
  }

  fun success(sender: CommandSender, message: String) {
    sender.sendMessage(PREFIX.append(Component.text(message, NamedTextColor.GREEN)))
  }

  fun warn(sender: CommandSender, message: String) {
    sender.sendMessage(PREFIX.append(Component.text(message, NamedTextColor.YELLOW)))
  }

  fun error(sender: CommandSender, message: String) {
    sender.sendMessage(PREFIX.append(Component.text(message, NamedTextColor.RED)))
  }
}
