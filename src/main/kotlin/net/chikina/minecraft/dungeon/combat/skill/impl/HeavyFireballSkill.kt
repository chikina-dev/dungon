package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.ExplosionSkill
import net.chikina.minecraft.dungeon.item.GameMaterial
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LargeFireball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class HeavyFireballSkill : ExplosionSkill() {
    override val id: String = "heavy_fireball"
    override val name: String = "Heavy Fireball"
    override val cooldown: Long = 5000
    override val manaCost: Double = 20.0
    override val icon: ItemStack = ItemStack(Material.FIRE_CHARGE)
    override val unlockMaterial = GameMaterial.ORB_FIRE

    override fun perform(attacker: CombatEntity, target: CombatEntity?) {
        val le = attacker.getLivingEntity() ?: return
        val fireball = le.launchProjectile(LargeFireball::class.java)
        fireball.yield = 0.0f

        fireball.persistentDataContainer.set(PluginKeys.SKILL_ID, PersistentDataType.STRING, id)

        val dir = le.location.direction.multiply(1.5)
        fireball.velocity = dir

        DungeonTask.runTimer(1L, 1L) { task ->
            if (fireball.isDead || !fireball.isValid) {
                task.cancel()
                return@runTimer
            }
            val currentVel = fireball.velocity
            currentVel.add(Vector(0.0, -0.05, 0.0))
            fireball.velocity = currentVel
        }
    }

    override fun onProjectileHit(event: ProjectileHitEvent, attacker: CombatEntity) {
        val entity = event.hitEntity
        val block = event.hitBlock

        val loc: Location = entity?.location ?: block?.location ?: return

        explode(
                loc,
                attacker,
                30.0,
                1.5,
                4.0,
                true,
                Particle.EXPLOSION_EMITTER,
                1,
                0.0,
                Sound.ENTITY_GENERIC_EXPLODE,
                1f,
                0.8f
        )
    }

    override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
        return emptyList()
    }
}
