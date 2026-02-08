package net.chikina.minecraft.dungeon.player

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerManager {
  private val players = ConcurrentHashMap<UUID, DungeonPlayer>()

  fun getPlayer(player: Player): DungeonPlayer = players.computeIfAbsent(player.uniqueId) { DungeonPlayer(player) }

  fun removePlayer(uuid: UUID) {
    players.remove(uuid)
  }

  fun tickAll() {
    players.values.forEach { it.tick() }
  }
}
