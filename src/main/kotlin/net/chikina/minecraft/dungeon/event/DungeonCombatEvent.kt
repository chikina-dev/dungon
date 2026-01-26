package net.chikina.minecraft.dungeon.event

import net.chikina.minecraft.dungeon.combat.CombatEntity
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent

class DungeonCombatEvent(
        val attacker: CombatEntity,
        val victim: CombatEntity,
        val originalEvent: EntityDamageByEntityEvent
) : DungeonEvent(), Cancellable {

  companion object {
    private val HANDLERS = HandlerList()

    @JvmStatic
    fun getHandlerList(): HandlerList {
      return HANDLERS
    }
  }

  override fun getHandlers(): HandlerList {
    return HANDLERS
  }

  override fun isCancelled(): Boolean {
    return originalEvent.isCancelled
  }

  override fun setCancelled(cancel: Boolean) {
    originalEvent.isCancelled = cancel
  }
}
