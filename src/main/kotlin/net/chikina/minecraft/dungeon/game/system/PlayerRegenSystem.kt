package net.chikina.minecraft.dungeon.game.system

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.game.GameSystem
import net.chikina.minecraft.dungeon.player.PlayerManager

class PlayerRegenSystem(
  private val playerManager: PlayerManager,
) : GameSystem {
  override fun update() {
    Dungeon.instance.server.onlinePlayers.forEach { player ->
      val dungeonPlayer = playerManager.getPlayer(player)
      dungeonPlayer.regenerateMana()
      dungeonPlayer.regenerateHealth()
    }
  }
}
