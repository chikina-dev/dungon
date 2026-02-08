package net.chikina.minecraft.dungeon.combat

data class DamageContext(
  var amount: Double,
  val type: DamageType,
  val attacker: CombatEntity? = null,
  val isCrit: Boolean = false,
  var poiseDamage: Double = 0.0,
)
