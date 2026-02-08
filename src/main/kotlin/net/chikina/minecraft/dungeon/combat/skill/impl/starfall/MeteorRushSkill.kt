package net.chikina.minecraft.dungeon.combat.skill.impl.starfall

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.util.DungeonTask
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import java.util.UUID

class MeteorRushSkill : Skill() {
  override val id: String = "starfall_meteor_rush"
  override val icon: ItemStack = ItemStack(Material.NETHER_STAR)
  override val name: String = "Meteor Rush"
  override val cooldown: Long = 100

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val le = attacker.getLivingEntity() ?: return
    val direction = le.location.direction
      .normalize()
      .multiply(1.5 + (level * 0.2))

    le.velocity = direction.setY(0.2)

    val hitEntities = mutableSetOf<UUID>()

    var tick = 0
    DungeonTask.runTimer(0L, 2L) { task ->
      if (tick > 10 || le.isDead || (le.isOnGround && tick > 2)) {
        task.cancel()
        return@runTimer
      }

      val multiplier = 1.5
      val damage = attacker.stats.attack.baseAttack * multiplier
      val nearby = attacker.getNearbyEntities(1.5)
      for (ce in nearby) {
        val e = ce.getLivingEntity() ?: continue
        if (e == le) continue
        if (hitEntities.contains(e.uniqueId)) continue

        applyDamage(ce, DamageContext(damage, DamageType.PHYSICAL, attacker, false))
        hitEntities.add(e.uniqueId)
        e.velocity = direction
          .clone()
          .normalize()
          .multiply(0.5)
          .setY(0.5)
      }

      le.world.spawnParticle(Particle.FLAME, le.location, 5, 0.2, 0.2, 0.2, 0.05)

      tick++
    }
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> = emptyList()
}
