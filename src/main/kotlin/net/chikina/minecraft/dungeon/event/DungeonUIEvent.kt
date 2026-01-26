package net.chikina.minecraft.dungeon.event

import net.chikina.minecraft.dungeon.ui.DungeonUI
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class DungeonUIClickEvent(
        val ui: DungeonUI,
        val player: Player,
        val slot: Int,
        val originalEvent: InventoryClickEvent
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

class DungeonUICloseEvent(
        val ui: DungeonUI,
        val player: Player,
        val originalEvent: InventoryCloseEvent
) : DungeonEvent() {

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
}
