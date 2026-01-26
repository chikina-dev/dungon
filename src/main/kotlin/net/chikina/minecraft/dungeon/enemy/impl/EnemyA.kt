package net.chikina.minecraft.dungeon.enemy.impl

import net.chikina.minecraft.dungeon.enemy.DungeonEnemy
import net.chikina.minecraft.dungeon.enemy.EnemyDrop
import net.chikina.minecraft.dungeon.enemy.EnemyEquipment
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.GameMaterial
import org.bukkit.Material
import org.bukkit.entity.EntityType

class EnemyA : DungeonEnemy("Scrap A", EntityType.SKELETON, 2000L) {
    override val baseRunes: Long = 10

    override fun initializeStats() {
        stats.hp = 100.0
        stats.attack.baseAttack = 15.0
        stats.defense.baseDefense = 5.0
        stats.speed = 0.25
    }

    override fun getEquipment(): EnemyEquipment {
        return EnemyEquipment.create(mainHand = GameItem(Material.WOODEN_SWORD))
    }

    override fun getDrops(): List<EnemyDrop> {
        return listOf(
                EnemyDrop(GameItem(Material.ROTTEN_FLESH), 1.0, 1, 2),
                EnemyDrop(GameItem.from(GameMaterial.TOKEN_A, 1), 0.005, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.ORB_HEALING, 1), 0.01, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.ORB_HOOK, 1), 0.01, 1, 1)
        )
    }
}
