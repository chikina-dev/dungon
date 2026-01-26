package net.chikina.minecraft.dungeon.event

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerAnimationEvent

class DungeonMiningSwingEvent(
        val player: Player,
        val block: Block,
        val originalEvent: PlayerAnimationEvent
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

class DungeonBlockBreakEvent(
        val player: Player,
        val block: Block,
        val originalEvent: BlockBreakEvent
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
