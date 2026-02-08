package net.chikina.minecraft.dungeon.combat.skill.funnel

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.VectorUtils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

enum class FunnelState {
  IDLE,
  FLYING,
  ATTACKING,
  RETURNING,
  DESPAWN,
}

class Funnel(
  val owner: CombatEntity,
  startLocation: Location,
  val durationTick: Long = 600,
) {
  private var entity: ArmorStand? = null
  var state: FunnelState = FunnelState.IDLE
  private var target: CombatEntity? = null
  private var age: Long = 0
  private var task: BukkitTask? = null

  var flySpeed: Double = 0.8
  var damage: Double = 10.0
  var knockbackForce: Double = 0.4

  init {
    spawn(startLocation)
    startTicking()
  }

  private fun spawn(loc: Location) {
    val world = loc.world ?: return

    entity = world.spawnEntity(loc.clone().subtract(0.0, 0.7, 0.0), EntityType.ARMOR_STAND) as? ArmorStand
    entity?.let {
      it.isVisible = false
      it.isSmall = true
      it.setGravity(false)
      it.isMarker = true
      it.equipment.helmet = ItemStack(Material.NETHER_STAR)
    }
  }

  private fun startTicking() {
    task =
      DungeonTask.runTimer(1L, 1L) { task ->
        if (entity == null || !entity!!.isValid || owner.isDead || age >= durationTick || state == FunnelState.DESPAWN) {
          remove()
          task.cancel()
          return@runTimer
        }

        tick()
        age++
      }
  }

  private fun tick() {
    val loc = entity?.location ?: return
    val headLoc = loc.clone().add(0.0, 0.7, 0.0)

    if (state == FunnelState.IDLE) {
      val offset =
        Vector(
          Math.cos(age * 0.1) * 1.5,
          Math.sin(age * 0.05) * 0.5 + 2.0,
          Math.sin(age * 0.1) * 1.5,
        )
      val desiredLoc = owner.location
        .clone()
        .add(offset)
        .subtract(0.0, 0.7, 0.0)

      val dir = VectorUtils.getDirection(loc, desiredLoc)
      if (loc.distanceSquared(desiredLoc) > 0.1) {
        val newLoc = VectorUtils.moveTowards(loc, desiredLoc, 0.2)
        newLoc.yaw = (newLoc.yaw + 5) % 360
        entity?.teleport(newLoc)
      }
    }

    if (state == FunnelState.FLYING && target != null) {
      if (target!!.isDead || target!!.getLivingEntity()?.isValid != true) {
        state = FunnelState.DESPAWN
        return
      }

      val targetLoc = target!!.location.clone().add(0.0, 1.0, 0.0)
      val dist = headLoc.distance(targetLoc)
      val dir = VectorUtils.getDirection(headLoc, targetLoc)

      if (dist < 1.0) {
        attack(target!!)
        state = FunnelState.DESPAWN
      } else {
        val newLoc = VectorUtils.moveTowards(headLoc, targetLoc, flySpeed)
        newLoc.subtract(0.0, 0.7, 0.0)

        newLoc.direction = dir
        entity?.teleport(newLoc)
        headLoc.world.spawnParticle(Particle.END_ROD, headLoc, 1, 0.0, 0.0, 0.0, 0.0)
      }
    }
  }

  fun launch(target: CombatEntity) {
    this.target = target
    this.state = FunnelState.FLYING
  }

  private fun attack(target: CombatEntity) {
    target.takeDamage(DamageContext(damage, DamageType.MAGIC, owner))

    target.location.world.spawnParticle(
      Particle.EXPLOSION,
      target.location.add(0.0, 1.0, 0.0),
      3,
      0.2,
      0.2,
      0.2,
      0.0,
    )
    target.location.world.playSound(target.location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f)

    val funLoc = entity?.location?.add(0.0, 0.7, 0.0) ?: return
    val dir = target.location
      .toVector()
      .subtract(funLoc.toVector())
      .normalize()

    val kb = dir.multiply(knockbackForce).setY(knockbackForce * 0.5)
    target.getLivingEntity()?.velocity = target.getLivingEntity()?.velocity?.add(kb) ?: kb
  }

  fun remove() {
    entity?.remove()
    entity = null
    state = FunnelState.DESPAWN
    task?.cancel()
  }
}
