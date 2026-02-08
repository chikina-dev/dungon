package net.chikina.minecraft.dungeon.event

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.Skill
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.ProjectileHitEvent

class DungeonProjectileHitEvent(
  val projectile: Projectile,
  val shooter: CombatEntity,
  val skill: Skill?,
  val originalEvent: ProjectileHitEvent,
) : DungeonEvent(),
  Cancellable {
  companion object {
    private val HANDLERS = HandlerList()

    @JvmStatic
    fun getHandlerList(): HandlerList = HANDLERS
  }

  override fun getHandlers(): HandlerList = HANDLERS

  override fun isCancelled(): Boolean = originalEvent.isCancelled

  override fun setCancelled(cancel: Boolean) {
    originalEvent.isCancelled = cancel
  }
}
