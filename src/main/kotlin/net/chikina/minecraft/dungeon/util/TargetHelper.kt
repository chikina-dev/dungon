package net.chikina.minecraft.dungeon.util

import net.chikina.minecraft.dungeon.combat.CombatEntity
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult

object TargetHelper {

    fun rayTrace(attacker: LivingEntity, range: Double, radius: Double): CombatEntity? {
        val eyeLoc = attacker.eyeLocation
        val direction = eyeLoc.direction

        val world = attacker.world
        val result: RayTraceResult? =
                world.rayTraceEntities(eyeLoc, direction, range, radius) { entity ->
                    entity != attacker && isValidTarget(entity, attacker)
                }

        val hitEntity = result?.hitEntity ?: return null
        return CombatEntity.from(hitEntity)
    }

    fun getNearbyEnemies(
            center: Location,
            range: Double,
            attacker: CombatEntity? = null
    ): List<CombatEntity> {
        return CombatEntity.getNearbyEntities(center, range).filter { combatEntity ->
            val entity = combatEntity.getLivingEntity() ?: return@filter false
            if (attacker != null && entity == attacker.getLivingEntity()) return@filter false
            isValidTarget(entity, attacker?.getLivingEntity())
        }
    }

    fun getChainTarget(
            current: CombatEntity,
            hitList: Set<CombatEntity>,
            range: Double,
            attacker: CombatEntity
    ): CombatEntity? {
        val center = current.location
        val nearby = getNearbyEnemies(center, range, attacker)

        return nearby.filter { !hitList.contains(it) && it != current }.minByOrNull {
            it.location.distanceSquared(center)
        }
    }

    fun isValidTarget(target: Entity, source: LivingEntity?): Boolean {
        if (target !is LivingEntity) return false
        if (target.isDead) return false
        if (!target.isValid) return false

        if (target is ArmorStand) return false

        if (target is Player &&
                        (target.gameMode == GameMode.SPECTATOR ||
                                target.gameMode == GameMode.CREATIVE)
        )
                return false

        if (source != null) {
            val combatTarget = CombatEntity.from(target)
            if (combatTarget != null) {
                if (CombatEntity.from(source)?.isAlly(combatTarget) == true) {
                    return false
                }
            }
        }

        return true
    }
}
