package net.chikina.minecraft.dungeon.combat.effect.attribute

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.MagicElement
import org.bukkit.Particle

class WaterAttributeEffect(
  duration: Double,
  amount: Double,
  source: CombatEntity? = null,
) : AttributeEffect(duration, MagicElement.WATER, amount, source) {
  override fun playEffect() {
    val loc = owner.location.clone().add(0.0, 1.0, 0.0)
    val count = (amount / 5).toInt().coerceAtLeast(1).coerceAtMost(10)
    loc.world.spawnParticle(Particle.CLOUD, loc, count, 0.3, 0.5, 0.3, 0.1)
  }
}
