package net.chikina.minecraft.dungeon.database.repository.impl

import net.chikina.minecraft.dungeon.database.Database
import net.chikina.minecraft.dungeon.database.repository.ExplorationDungeonRepository
import net.chikina.minecraft.dungeon.map.ExplorationDungeon

class SqlExplorationDungeonRepository(
  private val database: Database,
) : ExplorationDungeonRepository {
  override fun findAll(): List<ExplorationDungeon> {
    val query =
      "SELECT id, name, x1, y1, z1, x2, y2, z2, world_name, seed, last_updated FROM exploration_dungeons"
    val dungeons = mutableListOf<ExplorationDungeon>()

    database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        val rs = stmt.executeQuery()
        while (rs.next()) {
          dungeons.add(
            ExplorationDungeon(
              id = rs.getInt("id"),
              name = rs.getString("name"),
              x1 = rs.getInt("x1"),
              y1 = rs.getInt("y1"),
              z1 = rs.getInt("z1"),
              x2 = rs.getInt("x2"),
              y2 = rs.getInt("y2"),
              z2 = rs.getInt("z2"),
              worldName = rs.getString("world_name"),
              seed = rs.getLong("seed"),
              lastUpdated = rs.getLong("last_updated"),
            ),
          )
        }
      }
    }
    return dungeons
  }

  override fun findByName(name: String): ExplorationDungeon? {
    val query =
      "SELECT id, name, x1, y1, z1, x2, y2, z2, world_name, seed, last_updated FROM exploration_dungeons WHERE name = ?"

    return database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        stmt.setString(1, name)
        val rs = stmt.executeQuery()
        if (rs.next()) {
          ExplorationDungeon(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            x1 = rs.getInt("x1"),
            y1 = rs.getInt("y1"),
            z1 = rs.getInt("z1"),
            x2 = rs.getInt("x2"),
            y2 = rs.getInt("y2"),
            z2 = rs.getInt("z2"),
            worldName = rs.getString("world_name"),
            seed = rs.getLong("seed"),
            lastUpdated = rs.getLong("last_updated"),
          )
        } else {
          null
        }
      }
    }
  }

  override fun save(dungeon: ExplorationDungeon) {
    val query =
      "INSERT INTO exploration_dungeons (name, x1, y1, z1, x2, y2, z2, world_name, seed, last_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        stmt.setString(1, dungeon.name)
        stmt.setInt(2, dungeon.x1)
        stmt.setInt(3, dungeon.y1)
        stmt.setInt(4, dungeon.z1)
        stmt.setInt(5, dungeon.x2)
        stmt.setInt(6, dungeon.y2)
        stmt.setInt(7, dungeon.z2)
        stmt.setString(8, dungeon.worldName)
        stmt.setLong(9, dungeon.seed)
        stmt.setLong(10, dungeon.lastUpdated)
        stmt.executeUpdate()
      }
    }
  }

  override fun update(dungeon: ExplorationDungeon) {
    val query =
      "UPDATE exploration_dungeons SET name = ?, x1 = ?, y1 = ?, z1 = ?, x2 = ?, y2 = ?, z2 = ?, world_name = ?, seed = ?, last_updated = ? WHERE id = ?"
    database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        stmt.setString(1, dungeon.name)
        stmt.setInt(2, dungeon.x1)
        stmt.setInt(3, dungeon.y1)
        stmt.setInt(4, dungeon.z1)
        stmt.setInt(5, dungeon.x2)
        stmt.setInt(6, dungeon.y2)
        stmt.setInt(7, dungeon.z2)
        stmt.setString(8, dungeon.worldName)
        stmt.setLong(9, dungeon.seed)
        stmt.setLong(10, dungeon.lastUpdated)
        stmt.setInt(11, dungeon.id)
        stmt.executeUpdate()
      }
    }
  }
}
