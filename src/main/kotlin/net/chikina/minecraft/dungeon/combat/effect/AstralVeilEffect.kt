package net.chikina.minecraft.dungeon.combat.effect

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.skill.funnel.Funnel
import net.chikina.minecraft.dungeon.combat.skill.funnel.FunnelState
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.entity.Player

class AstralVeilEffect(
  level: Int,
  durationSeconds: Double = 30.0,
) : DungeonEffect(durationSeconds) {
  private val damageReduction = 0.3 + (level * 0.05)
  private val funnelCount = 3 + level

  override fun onDamageTaken(context: DamageContext): Double {
    if (context.attacker != null && !context.attacker.isDead) {
      spawnReactiveFunnels(context.attacker)
    }

    owner.location.world.spawnParticle(Particle.ENCHANT, owner.location.add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5)

    return 1.0 - damageReduction
  }

  override fun onRemove() {
    owner.location.world.getNearbyEntities(owner.location, 50.0, 50.0, 50.0).forEach {
      if (it is Player) {
        it.sendMessage(Component.text("The Astral Veil has faded!", NamedTextColor.YELLOW))
      }
    }
  }

  private fun spawnReactiveFunnels(target: CombatEntity) {
    val loc = owner.location.clone().add(0.0, 2.5, 0.0)

    val funnel = Funnel(owner, loc, 100L)
    funnel.flySpeed = 1.0
    funnel.knockbackForce = 0.5

    DungeonTask.runLater(5L) {
      if (funnel.state == FunnelState.DESPAWN) return@runLater
      funnel.launch(target)
    }
  }
}
