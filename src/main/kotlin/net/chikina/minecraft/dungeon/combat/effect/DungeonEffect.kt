package net.chikina.minecraft.dungeon.combat.effect

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType

abstract class DungeonEffect(
  open var durationSeconds: Double,
  val isPersistent: Boolean = false,
) {
  open fun merge(newEffect: DungeonEffect): Boolean = false

  lateinit var owner: CombatEntity

  var remainingTicks: Long =
    if (durationSeconds == Double.MAX_VALUE) {
      Long.MAX_VALUE
    } else {
      (durationSeconds * 20).toLong()
    }
  var isExpired: Boolean = false

  open fun onApply() {}

  open fun onTick() {
    if (remainingTicks != Long.MAX_VALUE) {
      remainingTicks--
      if (remainingTicks <= 0) {
        isExpired = true
        onRemove()
      }
    }
  }

  open fun onRemove() {}

  open fun onDamageTaken(context: DamageContext): Double = 1.0

  open fun onAttack(target: CombatEntity, damage: Double, type: DamageType) {}

  open fun onLogout() {}

  open fun onLogin() {}

  open fun onDeath() {}

  open fun onRespawn() {}
}
