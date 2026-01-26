package net.chikina.minecraft.dungeon.enemy

import net.chikina.minecraft.dungeon.enemy.impl.*

class DefaultEnemyFactory : EnemyFactory {
    override fun create(type: String): DungeonEnemy {
        return when (type.uppercase()) {
            "A" -> EnemyA()
            "B" -> EnemyB()
            "C" -> EnemyC()
            "D" -> EnemyD()
            "E" -> EnemyE()
            "DUMMY" -> DamageDummy()
            else -> EnemyA()
        }
    }
}
