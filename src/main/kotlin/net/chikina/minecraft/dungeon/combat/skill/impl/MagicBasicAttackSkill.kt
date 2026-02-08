package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageCalculator
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.MagicElement
import net.chikina.minecraft.dungeon.combat.effect.attribute.EarthAttributeEffect
import net.chikina.minecraft.dungeon.combat.effect.attribute.FireAttributeEffect
import net.chikina.minecraft.dungeon.combat.effect.attribute.ThunderAttributeEffect
import net.chikina.minecraft.dungeon.combat.effect.attribute.WaterAttributeEffect
import net.chikina.minecraft.dungeon.combat.effect.attribute.WindAttributeEffect
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.SkillRequirement
import net.chikina.minecraft.dungeon.combat.skill.WeaponRequirement
import net.chikina.minecraft.dungeon.combat.skill.WeaponType
import net.chikina.minecraft.dungeon.item.WandItem
import net.chikina.minecraft.dungeon.util.ParticleUtils
import net.chikina.minecraft.dungeon.util.TargetHelper
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class MagicBasicAttackSkill : Skill() {
  override val id: String = "magic_basic_attack"
  override val icon: ItemStack = ItemStack(Material.STICK)
  override val name: String = "Magic Basic Attack"
  override val cooldown: Long = 1000
  override val manaCost: Double = 1.0
  override val dependency: SkillRequirement = WeaponRequirement(WeaponType.WAND)

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val livingAttacker = attacker.getLivingEntity() ?: return

    val targets = if (target != null) listOf(target) else getTargets(attacker)

    val handItem = livingAttacker.equipment?.itemInMainHand
    val wand = if (handItem != null) WandItem(handItem) else null
    val element = wand?.element ?: return

    val start = livingAttacker.eyeLocation.subtract(0.0, 0.2, 0.0)
    val direction = livingAttacker.location.direction

    val range = 20.0
    val end = start.clone().add(direction.clone().multiply(range))

    val distance = start.distance(end)
    val step = 0.5
    var current = 0.0
    val particleColor =
      when (element) {
        MagicElement.FIRE -> Color.RED
        MagicElement.WATER -> Color.BLUE
        MagicElement.THUNDER -> Color.YELLOW
        MagicElement.WIND -> Color.LIME
        MagicElement.EARTH -> Color.ORANGE
      }

    val dustOptions = Particle.DustOptions(particleColor, 1.0f)

    ParticleUtils.drawLine(start, end, Particle.DUST, 1, step, data = dustOptions)

    if (targets.isEmpty()) return

    for (t in targets) {
      val damageResult = DamageCalculator.calculateDamage(attacker, t)

      applyDamage(
        t,
        DamageContext(
          damageResult.totalDamage,
          DamageType.MAGIC,
          attacker,
          damageResult.isCrit,
        ),
      )

      if (attacker.isAlly(t)) continue

      val faith = attacker.stats.faith
      val chance = 0.3 + (faith * 0.005)
      if (Random.nextDouble() < chance) {
        val amount = (5.0 + (faith * 0.5)).coerceAtLeast(1.0)
        val duration = 5.0

        val effect =
          when (element) {
            MagicElement.FIRE -> {
              FireAttributeEffect(duration, amount, attacker)
            }

            MagicElement.WATER -> {
              WaterAttributeEffect(duration, amount, attacker)
            }

            MagicElement.THUNDER -> {
              ThunderAttributeEffect(duration, amount, attacker)
            }

            MagicElement.WIND -> {
              WindAttributeEffect(duration, amount, attacker)
            }

            MagicElement.EARTH -> {
              EarthAttributeEffect(duration, amount, attacker)
            }
          }
        t.addEffect(effect)
      }
    }
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
    val livingEntity = attacker.getLivingEntity() ?: return emptyList()
    val range = 20.0

    val result =
      livingEntity.world.rayTraceEntities(
        livingEntity.eyeLocation,
        livingEntity.location.direction,
        range,
        0.5,
      ) { it != livingEntity && TargetHelper.isValidTarget(it, livingEntity) }

    val hitEntity = result?.hitEntity

    if (hitEntity != null) {
      val combatEntity = CombatEntity.from(hitEntity)
      if (combatEntity != null) return listOf(combatEntity)
    }

    return emptyList()
  }
}
