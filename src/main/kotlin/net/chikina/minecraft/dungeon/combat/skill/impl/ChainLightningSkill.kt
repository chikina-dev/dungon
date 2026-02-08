package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.item.GameMaterial
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.TargetHelper
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

class ChainLightningSkill : Skill() {
  override val id: String = "chain_lightning"
  override val name: String = "Chain Lightning"
  override val cooldown: Long = 8000
  override val manaCost: Double = 30.0
  override val icon: ItemStack = ItemStack(Material.LIGHTNING_ROD)
  override val unlockMaterial = GameMaterial.ORB_THUNDER

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val le = attacker.getLivingEntity() ?: return

    val firstTarget = TargetHelper.rayTrace(le, 20.0, 1.5)

    if (firstTarget == null) {
      attacker.sendMessage("§cNo target found.")
      return
    }

    val magicPower =
      attacker.stats.attack.baseAttack * (1.0 + attacker.stats.attack.magicAttack / 100.0)
    val context = DamageContext(25.0 + (magicPower * 1.0), DamageType.MAGIC, attacker, true)
    val hitEntities = HashSet<CombatEntity>()
    hitEntities.add(attacker)

    chain(firstTarget, hitEntities, context, 3, le.eyeLocation)
  }

  private fun chain(
    target: CombatEntity,
    hitEntities: HashSet<CombatEntity>,
    context: DamageContext,
    bounces: Int,
    sourceLoc: Location,
  ) {
    if (bounces < 0) return

    applyDamage(target, context)
    hitEntities.add(target)

    spawnParticleLine(sourceLoc, target.location.add(0.0, 1.0, 0.0))
    target.location.world?.playSound(
      target.location,
      Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
      0.5f,
      2.0f,
    )

    DungeonTask.runLater(5L) {
      val next = findNextTarget(target, hitEntities, context)
      if (next != null) {
        chain(next, hitEntities, context, bounces - 1, target.location.add(0.0, 1.0, 0.0))
      }
    }
  }

  private fun findNextTarget(
    current: CombatEntity,
    hitEntities: HashSet<CombatEntity>,
    context: DamageContext,
  ): CombatEntity? {
    val attacker = context.attacker ?: return null
    return TargetHelper.getChainTarget(current, hitEntities, 12.0, attacker)
  }

  private fun spawnParticleLine(start: Location, end: Location) {
    val distance = start.distance(end)
    val vector = end
      .clone()
      .subtract(start)
      .toVector()
      .normalize()
      .multiply(0.2)
    val points = (distance / 0.2).toInt()

    var current = start.clone()
    for (i in 0 until points) {
      current.add(vector)
      current.world?.spawnParticle(Particle.ELECTRIC_SPARK, current, 1, 0.0, 0.0, 0.0, 0.0)
    }
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> = emptyList()
}
