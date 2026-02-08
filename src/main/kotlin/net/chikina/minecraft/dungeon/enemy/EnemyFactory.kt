package net.chikina.minecraft.dungeon.enemy

fun interface EnemyFactory {
  fun create(type: String): DungeonEnemy
}
