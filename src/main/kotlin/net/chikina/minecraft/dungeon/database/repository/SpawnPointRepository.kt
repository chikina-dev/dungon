package net.chikina.minecraft.dungeon.database.repository

import net.chikina.minecraft.dungeon.enemy.SpawnPoint

interface SpawnPointRepository {
  fun loadAll(): List<SpawnPoint>

  fun save(spawnPoint: SpawnPoint): SpawnPoint

  fun delete(id: Int)

  fun clear()
}
