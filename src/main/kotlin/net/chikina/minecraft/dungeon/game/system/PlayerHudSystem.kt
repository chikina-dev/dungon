package net.chikina.minecraft.dungeon.game.system

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.game.GameSystem
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.player.PlayerManager
import net.chikina.minecraft.dungeon.stats.CombatStats
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import kotlin.math.floor

class PlayerHudSystem(
  private val playerManager: PlayerManager,
) : GameSystem {
  override fun update() {
    for (player in Dungeon.instance.server.onlinePlayers) {
      val dungeonPlayer = playerManager.getPlayer(player)
      val stats = dungeonPlayer.stats
      val item = player.inventory.itemInMainHand

      if (item.type == Material.AIR) {
        sendWeaponStats(dungeonPlayer, stats)
        continue
      }

      if (item.type.name.endsWith("_PICKAXE")) {
        val miningStats = dungeonPlayer.miningEntity.miningStats

        var timeText = ""
        if (Dungeon.instance.isMiningSystemEnabled) {
          val miningInfo = Dungeon.instance.miningManager.getMiningInfo(player)
          if (miningInfo != null) {
            val elapsed = System.currentTimeMillis() - miningInfo.startTime
            val remaining = (miningInfo.durationMillis - elapsed) / 1000.0
            if (remaining > 0) {
              val durationSeconds = miningInfo.durationMillis / 1000.0
              timeText = " | 時間: ${String.format("%.2f", durationSeconds)}秒"
            }
          }
        }

        val component =
          Component
            .text()
            .append(Component.text("採掘力: ${miningStats.breakingPower}", NamedTextColor.YELLOW))
            .append(Component.text(" | ", NamedTextColor.GRAY))
            .append(Component.text("幸運: ${miningStats.fortune}", NamedTextColor.YELLOW))
            .append(Component.text(timeText, NamedTextColor.YELLOW))
            .build()

        player.sendActionBar(component)
      } else {
        sendWeaponStats(dungeonPlayer, stats)
      }
    }
  }

  private fun sendWeaponStats(dungeonPlayer: DungeonPlayer, stats: CombatStats) {
    val currentHp = floor(dungeonPlayer.currentHp).toInt()
    val maxHp = floor(stats.hp).toInt()
    val defense = floor(stats.defense.baseDefense).toInt()
    val currentMana = floor(dungeonPlayer.currentMana).toInt()
    val maxMana = floor(stats.maxMana).toInt()

    val component =
      Component
        .text()
        .append(Component.text("HP: $currentHp/$maxHp", NamedTextColor.RED))
        .append(Component.text("   ", NamedTextColor.GRAY))
        .append(Component.text("防御: $defense", NamedTextColor.GREEN))
        .append(Component.text("   ", NamedTextColor.GRAY))
        .append(Component.text("マナ: $currentMana/$maxMana", NamedTextColor.AQUA))
        .build()

    dungeonPlayer.player.sendActionBar(component)
  }
}
