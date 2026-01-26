package net.chikina.minecraft.dungeon.enemy.impl

import net.chikina.minecraft.dungeon.enemy.DungeonEnemy
import net.chikina.minecraft.dungeon.enemy.EnemyDrop
import net.chikina.minecraft.dungeon.enemy.EnemyEquipment
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.GameMaterial
import org.bukkit.Material
import org.bukkit.entity.EntityType

class EnemyC : DungeonEnemy("Scrap C", EntityType.SKELETON, 2000L) {
    override val baseRunes: Long = 25

    override fun initializeStats() {
        stats.hp = 400.0
        stats.attack.baseAttack = 40.0
        stats.defense.baseDefense = 0.0
        stats.speed = 0.2
    }

    override fun getEquipment(): EnemyEquipment {
        return EnemyEquipment.create(
                mainHand = GameItem(Material.IRON_SWORD),
                chestplate = GameItem(Material.LEATHER_CHESTPLATE)
        )
    }

    override fun getDrops(): List<EnemyDrop> {
        return listOf(
                EnemyDrop(GameItem(Material.IRON_INGOT), 0.5, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.TOKEN_C, 1), 0.005, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.ORB_FIRE, 1), 0.01, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.ORB_ICE, 1), 0.01, 1, 1)
        )
    }
}
