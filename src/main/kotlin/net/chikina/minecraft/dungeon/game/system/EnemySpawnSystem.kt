package net.chikina.minecraft.dungeon.game.system

import net.chikina.minecraft.dungeon.enemy.EnemySpawner
import net.chikina.minecraft.dungeon.game.GameSystem

class EnemySpawnSystem(private val enemySpawner: EnemySpawner) : GameSystem {
  override fun onEnable() {
    enemySpawner.initialize()
  }

  override fun update() {
    enemySpawner.checkSpawns()
  }
}
