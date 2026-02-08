package net.chikina.minecraft.dungeon.database.repository.impl

import net.chikina.minecraft.dungeon.database.Database
import net.chikina.minecraft.dungeon.database.repository.SpawnPointRepository
import net.chikina.minecraft.dungeon.enemy.SpawnPoint
import org.bukkit.Bukkit
import org.bukkit.Location

class SqlSpawnPointRepository(
  private val database: Database,
) : SpawnPointRepository {
  override fun loadAll(): List<SpawnPoint> {
    val query = "SELECT id, world, x, y, z, type FROM enemy_spawn_points"
    val points = mutableListOf<SpawnPoint>()

    database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        val rs = stmt.executeQuery()
        while (rs.next()) {
          val worldName = rs.getString("world")
          val world = Bukkit.getWorld(worldName)
          if (world != null) {
            val location =
              Location(
                world,
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
              )
            val id = rs.getInt("id")
            val type = rs.getString("type")
            points.add(SpawnPoint(id, location, type))
          }
        }
      }
    }
    return points
  }

  override fun save(spawnPoint: SpawnPoint): SpawnPoint {
    val query =
      "INSERT INTO enemy_spawn_points (world, x, y, z, type) VALUES (?, ?, ?, ?, ?) RETURNING id"

    database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        stmt.setString(1, spawnPoint.location.world.name)
        stmt.setDouble(2, spawnPoint.location.x)
        stmt.setDouble(3, spawnPoint.location.y)
        stmt.setDouble(4, spawnPoint.location.z)
        stmt.setString(5, spawnPoint.type)

        val rs = stmt.executeQuery()
        if (rs.next()) {
          val id = rs.getInt(1)
          return spawnPoint.copy(id = id)
        }
      }
    }
    throw IllegalStateException("Failed to save spawn point")
  }

  override fun delete(id: Int) {
    val query = "DELETE FROM enemy_spawn_points WHERE id = ?"

    database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        stmt.setInt(1, id)
        stmt.executeUpdate()
      }
    }
  }

  override fun clear() {
    val query = "DELETE FROM enemy_spawn_points"

    database.getConnection().use { conn ->
      conn.createStatement().use { stmt -> stmt.executeUpdate(query) }
    }
  }
}
