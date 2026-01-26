package net.chikina.minecraft.dungeon.database.repository

import java.util.*
import net.chikina.minecraft.dungeon.player.PlayerData

interface PlayerRepository {
    fun save(data: PlayerData)
    fun load(uuid: UUID): PlayerData
}
