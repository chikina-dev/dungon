package net.chikina.minecraft.dungeon.listener

import net.chikina.minecraft.dungeon.foraging.TreeManager
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class TreeListener : Listener {
  @EventHandler(priority = EventPriority.HIGHEST)
  fun onBlockBreak(event: BlockBreakEvent) {
    // Creative mode: Let vanilla handle it (instant break)
    if (event.player.gameMode == GameMode.CREATIVE) {
      if (TreeManager.handleBlockBreak(event.block)) {
        event.isCancelled = false
        event.isDropItems = true
      }
      return
    }

    // Survival mode: Handled by ForagingPlayer (simulated break).
    // Trees need to register the damage/break event, but we don't want vanilla break.
    if (TreeManager.handleBlockBreak(event.block)) {
      // Ensure we don't un-cancel for survival players handled by ForagingPlayer
      // event.isCancelled is likely true from MiningListener
    }
  }
}
