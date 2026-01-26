package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.ExplosionSkill
import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Fireball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class TestFireballSkill : ExplosionSkill() {
    override val id: String = "test_fireball"
    override val name: String = "Fireball"
    override val cooldown: Long = 2000
    override val manaCost: Double = 5.0
    override val icon: ItemStack = ItemStack(Material.FIRE_CHARGE)

    override fun perform(attacker: CombatEntity, target: CombatEntity?) {
        val le = attacker.getLivingEntity() ?: return
        val fireball = le.launchProjectile(Fireball::class.java)
        fireball.yield = 0.0f
        fireball.velocity = le.location.direction.multiply(2.0)

        fireball.persistentDataContainer.set(PluginKeys.SKILL_ID, PersistentDataType.STRING, id)
    }

    override fun onProjectileHit(event: ProjectileHitEvent, attacker: CombatEntity) {
        val entity = event.hitEntity
        val block = event.hitBlock

        val loc = entity?.location ?: block?.location ?: return

        explode(
                loc,
                attacker,
                20.0,
                1.0,
                3.0,
                true,
                Particle.EXPLOSION_EMITTER,
                1,
                0.0,
                Sound.ENTITY_GENERIC_EXPLODE,
                1.0f,
                1.2f
        )
    }

    override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
        return emptyList()
    }
}
