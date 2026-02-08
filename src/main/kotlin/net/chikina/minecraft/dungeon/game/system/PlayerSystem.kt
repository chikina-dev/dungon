package net.chikina.minecraft.dungeon.game.system

import net.chikina.minecraft.dungeon.game.GameSystem
import net.chikina.minecraft.dungeon.player.PlayerManager

class PlayerSystem(
  private val playerManager: PlayerManager,
) : GameSystem {
  override fun update() {
    playerManager.tickAll()
  }
}
