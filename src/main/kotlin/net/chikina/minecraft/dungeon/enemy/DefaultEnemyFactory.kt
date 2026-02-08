package net.chikina.minecraft.dungeon.enemy

import net.chikina.minecraft.dungeon.enemy.impl.DamageDummy
import net.chikina.minecraft.dungeon.enemy.impl.EnemyA
import net.chikina.minecraft.dungeon.enemy.impl.EnemyB
import net.chikina.minecraft.dungeon.enemy.impl.EnemyC
import net.chikina.minecraft.dungeon.enemy.impl.EnemyD
import net.chikina.minecraft.dungeon.enemy.impl.EnemyE

class DefaultEnemyFactory : EnemyFactory {
  override fun create(type: String): DungeonEnemy = when (type.uppercase()) {
    "A" -> EnemyA()
    "B" -> EnemyB()
    "C" -> EnemyC()
    "D" -> EnemyD()
    "E" -> EnemyE()
    "DUMMY" -> DamageDummy()
    else -> EnemyA()
  }
}
