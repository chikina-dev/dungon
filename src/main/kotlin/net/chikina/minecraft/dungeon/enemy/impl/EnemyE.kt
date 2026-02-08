package net.chikina.minecraft.dungeon.enemy.impl

import net.chikina.minecraft.dungeon.enemy.DungeonEnemy
import net.chikina.minecraft.dungeon.enemy.EnemyDrop
import net.chikina.minecraft.dungeon.enemy.EnemyEquipment
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.GameMaterial
import net.chikina.minecraft.dungeon.item.OreItem
import org.bukkit.Material
import org.bukkit.entity.EntityType

class EnemyE : DungeonEnemy("Scrap E", EntityType.SKELETON, 2000L) {
  override val baseRunes: Long = 100

  override fun initializeStats() {
    stats.hp = 500.0
    stats.attack.baseAttack = 50.0
    stats.defense.baseDefense = 20.0
    stats.speed = 0.25
  }

  override fun getEquipment(): EnemyEquipment = EnemyEquipment.create(
    mainHand = GameItem(Material.DIAMOND_SWORD),
    helmet = GameItem(Material.IRON_HELMET),
    chestplate = GameItem(Material.IRON_CHESTPLATE),
    leggings = GameItem(Material.IRON_LEGGINGS),
    boots = GameItem(Material.IRON_BOOTS),
  )

  override fun getDrops(): List<EnemyDrop> = listOf(
    EnemyDrop(OreItem.MITHRIL_ORE, 0.1, 1, 1),
    EnemyDrop(GameItem.from(GameMaterial.TOKEN_E, 1), 0.005, 1, 1),
    EnemyDrop(GameItem.from(GameMaterial.ORB_VOID, 1), 0.01, 1, 1),
  )
}
