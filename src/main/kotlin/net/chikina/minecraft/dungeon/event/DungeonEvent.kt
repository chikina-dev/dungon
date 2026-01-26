package net.chikina.minecraft.dungeon.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class DungeonEvent : Event() {
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
