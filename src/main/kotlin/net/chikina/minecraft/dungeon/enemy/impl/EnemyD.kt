package net.chikina.minecraft.dungeon.enemy.impl

import net.chikina.minecraft.dungeon.enemy.DungeonEnemy
import net.chikina.minecraft.dungeon.enemy.EnemyDrop
import net.chikina.minecraft.dungeon.enemy.EnemyEquipment
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.GameMaterial
import org.bukkit.Material
import org.bukkit.entity.EntityType

class EnemyD : DungeonEnemy("Scrap D", EntityType.SKELETON, 2000L) {
    override val baseRunes: Long = 50

    override fun initializeStats() {
        stats.hp = 300.0
        stats.attack.baseAttack = 60.0
        stats.defense.baseDefense = 5.0
        stats.speed = 0.3
    }

    override fun getEquipment(): EnemyEquipment {
        return EnemyEquipment.create(
                mainHand = GameItem(Material.GOLDEN_SWORD),
                helmet = GameItem(Material.GOLDEN_HELMET),
                chestplate = GameItem(Material.GOLDEN_CHESTPLATE)
        )
    }

    override fun getDrops(): List<EnemyDrop> {
        return listOf(
                EnemyDrop(GameItem(Material.GOLD_INGOT), 0.3, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.TOKEN_D, 1), 0.005, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.ORB_THUNDER, 1), 0.01, 1, 1)
        )
    }
}
