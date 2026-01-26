package net.chikina.minecraft.dungeon.mining.listener

import net.chikina.minecraft.dungeon.event.DungeonBlockBreakEvent
import net.chikina.minecraft.dungeon.event.DungeonMiningSwingEvent
import net.chikina.minecraft.dungeon.mining.core.MiningManager
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class MiningListener(private val miningManager: MiningManager) : Listener {

    @EventHandler
    fun onSwing(event: DungeonMiningSwingEvent) {
        miningManager.handleSwing(event.player, event.block)
    }

    @EventHandler
    fun onBreak(event: DungeonBlockBreakEvent) {
        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        event.isCancelled = true
        event.originalEvent.isDropItems = false
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        miningManager.removePlayer(event.player.uniqueId)
    }
}
