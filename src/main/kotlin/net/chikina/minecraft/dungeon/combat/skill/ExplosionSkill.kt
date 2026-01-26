package net.chikina.minecraft.dungeon.combat.skill

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound

abstract class ExplosionSkill : Skill() {

    /**
     * 指定した地点で爆発を起こします。
     *
     * @param center 爆発の中心
     * @param attacker 攻撃者
     * @param baseDamage 基礎ダメージ
     * @param magicScaling 魔法攻撃力の倍率 (例: 1.5 -> 魔攻 * 1.5)
     * @param radius ダメージ範囲 (半径)
     * @param isCrit クリティカルが発生するかどうか
     */
    protected fun explode(
            center: Location,
            attacker: CombatEntity,
            baseDamage: Double,
            magicScaling: Double,
            radius: Double,
            isCrit: Boolean = true,
            particle: Particle = Particle.EXPLOSION_EMITTER,
            particleCount: Int = 1,
            particleSpeed: Double = 0.0,
            sound: Sound = Sound.ENTITY_GENERIC_EXPLODE,
            soundVolume: Float = 1f,
            soundPitch: Float = 1f
    ) {
        val world = center.world ?: return

        world.spawnParticle(particle, center, particleCount, 0.0, 0.0, 0.0, particleSpeed)
        world.playSound(center, sound, soundVolume, soundPitch)

        world.spawnParticle(Particle.FLAME, center, 10, 0.5, 0.5, 0.5, 0.1)

        val magicPower =
                attacker.stats.attack.baseAttack * (1.0 + attacker.stats.attack.magicAttack / 100.0)
        val totalDamage = baseDamage + (magicPower * magicScaling)
        val context = DamageContext(totalDamage, DamageType.MAGIC, attacker, isCrit)

        val nearby = CombatEntity.getNearbyEntities(center, radius)
        val processedEntities = HashSet<CombatEntity>()

        for (combatEntity in nearby) {
            if (combatEntity.location.distanceSquared(center) > radius * radius) continue
            if (combatEntity == attacker || processedEntities.contains(combatEntity)) continue

            applyDamage(combatEntity, context)
            processedEntities.add(combatEntity)
        }
    }
}
