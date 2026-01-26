package net.chikina.minecraft.dungeon.event

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.input.PlayerAction
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerActionEvent(
        val player: DungeonPlayer,
        val action: PlayerAction,
        val target: CombatEntity?,
        val originalEvent: Event
) : DungeonEvent(), Cancellable {

  companion object {
    private val HANDLERS = HandlerList()

    @JvmStatic
    fun getHandlerList(): HandlerList {
      return HANDLERS
    }
  }

  private var isCancelled = false

  override fun getHandlers(): HandlerList {
    return HANDLERS
  }

  override fun isCancelled(): Boolean {
    return isCancelled
  }

  override fun setCancelled(cancel: Boolean) {
    this.isCancelled = cancel
  }
}
