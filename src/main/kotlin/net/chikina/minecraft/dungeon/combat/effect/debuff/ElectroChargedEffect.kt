package net.chikina.minecraft.dungeon.combat.effect.debuff

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.effect.DungeonEffect
import org.bukkit.Particle

class ElectroChargedEffect(
  duration: Double,
  val damagePerTick: Double,
  val source: CombatEntity?,
) : DungeonEffect(duration, isPersistent = false) {
  private val tickInterval = 20
  private var tickCounter = 0

  override fun merge(newEffect: DungeonEffect): Boolean {
    if (newEffect is ElectroChargedEffect) {
      if (newEffect.damagePerTick > this.damagePerTick) {
        return false
      }
      this.durationSeconds = newEffect.durationSeconds
      this.remainingTicks = (this.durationSeconds * 20).toLong()
      return true
    }
    return false
  }

  override fun onTick() {
    super.onTick()
    tickCounter++
    if (tickCounter >= tickInterval) {
      tickCounter = 0
      applyDamage()
    }
  }

  private fun applyDamage() {
    owner.takeDamage(DamageContext(damagePerTick, DamageType.MAGIC, source))
    owner.location.world.spawnParticle(Particle.CRIT, owner.location.add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1)
    owner.sendMessage("§e感電しています! §7(-${damagePerTick.toInt()})")
  }
}
