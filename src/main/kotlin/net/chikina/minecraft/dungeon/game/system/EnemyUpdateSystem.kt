package net.chikina.minecraft.dungeon.game.system

import net.chikina.minecraft.dungeon.enemy.EnemySpawner
import net.chikina.minecraft.dungeon.game.GameSystem

class EnemyUpdateSystem(private val enemySpawner: EnemySpawner) : GameSystem {
  override fun update() {
    enemySpawner.tickActiveEnemies()
  }
}
