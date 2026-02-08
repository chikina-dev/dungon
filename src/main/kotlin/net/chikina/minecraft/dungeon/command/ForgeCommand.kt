package net.chikina.minecraft.dungeon.command

import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.scoreboard.Team

class ForgeCommand : CommandExecutor {
  @Suppress("DEPRECATION")
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

    val location = sender.location
    val villager = location.world.spawnEntity(location, EntityType.VILLAGER) as Villager

    villager.customName(Component.text("Forge Shop", NamedTextColor.DARK_RED))
    villager.isCustomNameVisible = true
    try {
      villager.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED"))?.baseValue = 0.0
    } catch (e: Exception) {
      Messenger.warn(sender, "Could not set movement speed.")
    }
    villager.isCollidable = false
    villager.isInvulnerable = true

    val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    val teamName = "no_collision"
    val team = scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)

    team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
    team.addEntry(villager.uniqueId.toString())

    Messenger.success(sender, "Forge Shop spawned!")
    return true
  }
}
