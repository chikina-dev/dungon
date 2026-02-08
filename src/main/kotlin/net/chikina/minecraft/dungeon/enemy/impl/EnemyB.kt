package net.chikina.minecraft.dungeon.enemy.impl

import net.chikina.minecraft.dungeon.enemy.DungeonEnemy
import net.chikina.minecraft.dungeon.enemy.EnemyDrop
import net.chikina.minecraft.dungeon.enemy.EnemyEquipment
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.GameMaterial
import org.bukkit.Material
import org.bukkit.entity.EntityType

class EnemyB : DungeonEnemy("Scrap B", EntityType.SKELETON, 2000L) {
  override val baseRunes: Long = 15

  override fun initializeStats() {
    stats.hp = 200.0
    stats.attack.baseAttack = 25.0
    stats.defense.baseDefense = 10.0
    stats.speed = 0.23
  }

  override fun getEquipment(): EnemyEquipment = EnemyEquipment.create(
    mainHand = GameItem(Material.STONE_SWORD),
    helmet = GameItem(Material.LEATHER_HELMET),
  )

  override fun getDrops(): List<EnemyDrop> = listOf(
    EnemyDrop(GameItem(Material.BONE), 1.0, 1, 1),
    EnemyDrop(GameItem(Material.ARROW), 0.5, 1, 2),
    EnemyDrop(GameItem.from(GameMaterial.TOKEN_B, 1), 0.005, 1, 1),
    EnemyDrop(GameItem.from(GameMaterial.ORB_WIND, 1), 0.01, 1, 1),
  )
}
