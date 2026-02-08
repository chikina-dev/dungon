package net.chikina.minecraft.dungeon.combat.skill.impl.starfall

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.funnel.Funnel
import net.chikina.minecraft.dungeon.combat.skill.funnel.FunnelState
import net.chikina.minecraft.dungeon.util.DungeonTask

class StarBitSkill : Skill() {
  override val id: String = "starfall_star_bit"
  override val icon: org.bukkit.inventory.ItemStack =
    org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR)
  override val name: String = "Star-Bit"
  override val cooldown: Long = 100

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    if (target != null) listOf(target) else getTargets(attacker)

    val count = 2 + level

    for (i in 0 until count) {
      val offsetAngle = (360.0 / count) * i
      val loc =
        attacker.location
          .clone()
          .add(
            attacker.location.direction
              .multiply(0.5)
              .setY(0)
              .rotateAroundY(Math.toRadians(offsetAngle)),
          ).add(0.0, 1.5, 0.0)

      val funnel = Funnel(attacker, loc)
      funnel.flySpeed = 0.8 + (level * 0.1)

      val multiplier = 0.5
      funnel.damage = attacker.stats.attack.baseAttack * multiplier
      funnel.knockbackForce = 0.1

      DungeonTask.runLater(40L + (i * 5)) {
        if (funnel.state == FunnelState.DESPAWN) return@runLater

        val finalTarget = target ?: getTargets(attacker).randomOrNull()
        if (finalTarget != null) {
          funnel.launch(finalTarget)
        }
      }
    }
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> = attacker.getNearbyEntities(15.0).filter { it != attacker }
}
