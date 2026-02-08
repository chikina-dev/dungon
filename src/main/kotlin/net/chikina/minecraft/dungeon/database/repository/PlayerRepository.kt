package net.chikina.minecraft.dungeon.database.repository

import net.chikina.minecraft.dungeon.player.PlayerData
import java.util.UUID

interface PlayerRepository {
  fun save(data: PlayerData)

  fun load(uuid: UUID): PlayerData
}
