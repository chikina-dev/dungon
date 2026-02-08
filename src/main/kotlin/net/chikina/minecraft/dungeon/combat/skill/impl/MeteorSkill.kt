package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.ExplosionSkill
import net.chikina.minecraft.dungeon.item.GameMaterial
import net.chikina.minecraft.dungeon.util.DungeonTask
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

class MeteorSkill : ExplosionSkill() {
  override val id: String = "meteor"
  override val name: String = "Meteor Fall"
  override val cooldown: Long = 10000
  override val manaCost: Double = 40.0
  override val icon: ItemStack = ItemStack(Material.MAGMA_CREAM)
  override val unlockMaterial = GameMaterial.ORB_VOID

  private val maxRange = 25.0

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val le = attacker.getLivingEntity() ?: return

    val rayTrace =
      le.world.rayTrace(
        le.eyeLocation,
        le.location.direction,
        maxRange,
        FluidCollisionMode.NEVER,
        true,
        0.5,
      ) { entity -> entity != le }

    val targetLoc =
      if (rayTrace?.hitBlock != null) {
        rayTrace.hitBlock!!.location.add(0.5, 1.0, 0.5)
      } else if (rayTrace?.hitEntity != null) {
        rayTrace.hitEntity!!.location
      } else {
        le.eyeLocation.add(le.location.direction.multiply(maxRange))
      }

    targetLoc.world?.spawnParticle(Particle.FLAME, targetLoc, 20, 1.0, 0.1, 1.0, 0.05)
    DungeonTask.runLater(20L) {
      explode(
        targetLoc,
        attacker,
        50.0,
        2.0,
        5.0,
        true,
        Particle.EXPLOSION_EMITTER,
        1,
        0.0,
        Sound.ENTITY_GENERIC_EXPLODE,
        1f,
        0.5f,
      )
    }
    visualizeMeteor(targetLoc)
  }

  private fun visualizeMeteor(target: Location) {
    val start = target.clone().add(0.0, 20.0, 0.0)

    var ticks = 0
    DungeonTask.runTimer(0L, 1L) { task ->
      if (ticks >= 20) {
        task.cancel()
        return@runTimer
      }
      val progress = ticks / 20.0
      val startVec = start.toVector()
      val targetVec = target.toVector()
      val currentVec =
        startVec.clone().add(targetVec.clone().subtract(startVec).multiply(progress))

      val loc = currentVec.toLocation(target.world!!)
      loc.world?.spawnParticle(Particle.LAVA, loc, 5, 0.5, 0.5, 0.5)
      loc.world?.spawnParticle(Particle.FLAME, loc, 3, 0.2, 0.2, 0.2, 0.05)

      ticks++
    }
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> = emptyList()
}
