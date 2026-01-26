package net.chikina.minecraft.dungeon.enemy.impl

import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.enemy.DungeonEnemy
import net.chikina.minecraft.dungeon.enemy.EnemyDrop
import net.chikina.minecraft.dungeon.enemy.EnemyEquipment
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.util.Vector

class DamageDummy : DungeonEnemy("Target Dummy", EntityType.ZOMBIE, Long.MAX_VALUE) {
    override val baseRunes: Long = 0

    override fun initializeStats() {
        stats.hp = 1_000_000_000.0
        stats.attack.baseAttack = 0.0
        stats.defense.baseDefense = 0.0
        stats.speed = 0.0
    }

    override fun getEquipment(): EnemyEquipment {
        return EnemyEquipment.create()
    }

    override fun getDrops(): List<EnemyDrop> {
        return emptyList()
    }

    override fun spawn(location: Location) {
        super.spawn(location)

        entity?.let { mob ->
            if (mob is Mob) {
                mob.isAware = false
            }
            mob.setAI(false)
            mob.isCollidable = false
            mob.setGravity(true)
        }
    }

    override fun updateVisuals() {
        entity?.customName(Component.text("$name [HP: ${currentHp.toLong()}]"))
    }

    override fun onDamageTaken(context: DamageContext) {
        super.onDamageTaken(context)
        entity?.velocity = Vector(0, 0, 0)
    }
}
