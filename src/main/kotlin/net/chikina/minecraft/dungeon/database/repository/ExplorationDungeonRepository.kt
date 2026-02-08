package net.chikina.minecraft.dungeon.database.repository

import net.chikina.minecraft.dungeon.map.ExplorationDungeon

interface ExplorationDungeonRepository {
  fun findAll(): List<ExplorationDungeon>

  fun findByName(name: String): ExplorationDungeon?

  fun save(dungeon: ExplorationDungeon)

  fun update(dungeon: ExplorationDungeon)
}
