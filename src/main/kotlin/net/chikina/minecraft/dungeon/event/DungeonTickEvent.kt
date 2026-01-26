package net.chikina.minecraft.dungeon.event

import org.bukkit.event.HandlerList

class DungeonTickEvent(val tick: Long) : DungeonEvent() {

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
