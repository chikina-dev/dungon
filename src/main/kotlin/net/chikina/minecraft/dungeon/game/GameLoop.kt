package net.chikina.minecraft.dungeon.game

import net.chikina.minecraft.dungeon.event.DungeonTickEvent
import org.bukkit.Bukkit

object GameLoop {

  private data class SystemEntry(val system: GameSystem, val interval: Int)
  private val systems = mutableListOf<SystemEntry>()
  private var tickCount: Long = 0

  fun register(system: GameSystem, interval: Int = 1) {
    systems.add(SystemEntry(system, interval))
    system.onEnable()
  }

  fun run() {
    tickCount++

    systems.forEach { entry ->
      if (tickCount % entry.interval == 0L) {
        try {
          entry.system.update()
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    Bukkit.getPluginManager().callEvent(DungeonTickEvent(tickCount))
  }

  fun stop() {
    systems.forEach { it.system.onDisable() }
    systems.clear()
  }
}
