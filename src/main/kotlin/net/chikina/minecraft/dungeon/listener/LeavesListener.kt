package net.chikina.minecraft.dungeon.listener

import net.chikina.minecraft.dungeon.foraging.TreeManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.LeavesDecayEvent

class LeavesListener : Listener {
  @EventHandler
  fun onLeavesDecay(event: LeavesDecayEvent) {
    if (TreeManager.isTrackedLeaf(event.block)) {
      event.isCancelled = true
    }
  }
}
