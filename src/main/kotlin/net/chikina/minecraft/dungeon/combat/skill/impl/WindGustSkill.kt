package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.item.GameMaterial
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

class WindGustSkill : Skill() {
    override val id: String = "wind_gust"
    override val name: String = "Wind Gust"
    override val cooldown: Long = 3000
    override val manaCost: Double = 15.0
    override val icon: ItemStack = ItemStack(Material.FEATHER)
    override val unlockMaterial = GameMaterial.ORB_WIND

    override fun perform(attacker: CombatEntity, target: CombatEntity?) {
        val le = attacker.getLivingEntity() ?: return
        val loc = le.location
        val dir = loc.direction

        val targets = attacker.getNearbyEntities(6.0)
        val magicPower =
                attacker.stats.attack.baseAttack * (1.0 + attacker.stats.attack.magicAttack / 100.0)
        val context = DamageContext(15.0 + (magicPower * 0.8), DamageType.MAGIC, attacker, false)

        for (targetCombat in targets) {
            if (targetCombat == attacker) continue
            val entity = targetCombat.getLivingEntity() ?: continue

            val toEntity = entity.location.toVector().subtract(loc.toVector())
            if (toEntity.length() > 6.0) continue

            val angle = toEntity.normalize().angle(dir)
            if (angle < Math.toRadians(45.0)) {

                applyDamage(targetCombat, context)

                val velocity = dir.clone().multiply(1.5).setY(0.5)
                entity.velocity = velocity
            }
        }

        loc.world?.spawnParticle(Particle.CLOUD, loc.add(0.0, 1.0, 0.0), 20, 1.0, 0.5, 1.0, 0.5)
        loc.world?.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.5f)
    }

    override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
        return emptyList()
    }
}
