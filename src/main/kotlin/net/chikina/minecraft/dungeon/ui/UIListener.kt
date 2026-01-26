package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.event.DungeonUIClickEvent
import net.chikina.minecraft.dungeon.event.DungeonUICloseEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class UIListener : Listener {

    @EventHandler
    fun onInventoryClick(event: DungeonUIClickEvent) {
        event.ui.onClick(event.originalEvent)
    }

    @EventHandler
    fun onInventoryClose(event: DungeonUICloseEvent) {
        event.ui.onClose(event.originalEvent)
    }
}
