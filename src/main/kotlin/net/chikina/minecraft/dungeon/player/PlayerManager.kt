package net.chikina.minecraft.dungeon.player

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Player

class PlayerManager {

    private val players = ConcurrentHashMap<UUID, DungeonPlayer>()

    fun getPlayer(player: Player): DungeonPlayer {
        return players.computeIfAbsent(player.uniqueId) { DungeonPlayer(player) }
    }

    fun removePlayer(uuid: UUID) {
        players.remove(uuid)
    }

    fun tickAll() {
        players.values.forEach { it.tick() }
    }
}
