package net.chikina.minecraft.dungeon.combat.skill.impl.starfall

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.effect.VanillaEffect
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.funnel.Funnel
import net.chikina.minecraft.dungeon.util.DungeonTask
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

class JudgementStarfallSkill : Skill() {
  override val id: String = "starfall_judgement_starfall"
  override val icon: ItemStack = ItemStack(Material.NETHER_STAR)
  override val name: String = "Judgement: Starfall"
  override val cooldown: Long = 1200

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val finalTarget = target ?: getTargets(attacker).firstOrNull() ?: return

    finalTarget.addEffect(VanillaEffect(PotionEffectType.SLOWNESS, 50, 100))
    finalTarget.addEffect(VanillaEffect(PotionEffectType.JUMP_BOOST, 50, 200))

    var tick = 0
    DungeonTask.runTimer(0L, 5L) { task ->
      if (tick > 100 || finalTarget.isDead || finalTarget.getLivingEntity()?.isValid != true) {
        task.cancel()
        return@runTimer
      }

      finalTarget.location.world.spawnParticle(Particle.ENCHANTED_HIT, finalTarget.location.add(0.0, 1.0, 0.0), 10, 1.0, 1.0, 1.0)
      tick += 5
    }

    val count = 8 + (level * 2)
    for (i in 0 until count) {
      DungeonTask.runLater(10L + (i * 5)) {
        if (finalTarget.isDead || attacker.isDead) return@runLater

        val angle = Math.random() * 360
        val loc =
          finalTarget
            .location
            .clone()
            .add(
              finalTarget
                .location
                .direction
                .clone()
                .setY(0)
                .normalize()
                .multiply(10.0)
                .rotateAroundY(Math.toRadians(angle)),
            ).add(0.0, 6.0, 0.0)

        val funnel = Funnel(attacker, loc, 60L)
        funnel.flySpeed = 1.5
        funnel.damage = attacker.stats.attack.baseAttack * 3.0
        funnel.knockbackForce = 0.5

        funnel.launch(finalTarget)
      }
    }
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
    val targets = attacker.getNearbyEntities(10.0)
    return targets
      .filter { it != attacker }
      .sortedBy { it.location.distanceSquared(attacker.location) }
      .take(1)
  }
}
