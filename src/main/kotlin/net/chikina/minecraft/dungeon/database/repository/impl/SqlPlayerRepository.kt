package net.chikina.minecraft.dungeon.database.repository.impl

import net.chikina.minecraft.dungeon.database.Database
import net.chikina.minecraft.dungeon.database.repository.PlayerRepository
import net.chikina.minecraft.dungeon.player.PlayerData
import java.util.UUID

class SqlPlayerRepository(
  private val database: Database,
) : PlayerRepository {
  override fun save(data: PlayerData) {
    val query =
      """
      INSERT INTO player_data (uuid, runes, level, stat_points, allocations, equipped_skills, unlocked_skills)
      VALUES (?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT (uuid) DO UPDATE SET
          runes = EXCLUDED.runes,
          level = EXCLUDED.level,
          stat_points = EXCLUDED.stat_points,
          allocations = EXCLUDED.allocations,
          equipped_skills = EXCLUDED.equipped_skills,
          unlocked_skills = EXCLUDED.unlocked_skills;
      """.trimIndent()

    database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        stmt.setString(1, data.uuid.toString())
        stmt.setLong(2, data.runes)
        stmt.setInt(3, data.level)
        stmt.setInt(4, data.statPoints)
        stmt.setString(5, data.serializeAllocations())
        stmt.setString(6, data.serializeEquippedSkills())
        stmt.setString(7, data.serializeUnlockedSkills())
        stmt.executeUpdate()
      }
    }
  }

  override fun load(uuid: UUID): PlayerData {
    val query =
      "SELECT runes, level, stat_points, allocations, equipped_skills, unlocked_skills FROM player_data WHERE uuid = ?"

    return database.getConnection().use { conn ->
      conn.prepareStatement(query).use { stmt ->
        stmt.setString(1, uuid.toString())
        val rs = stmt.executeQuery()
        if (rs.next()) {
          PlayerData(
            uuid = uuid,
            runes = rs.getLong("runes"),
            level = rs.getInt("level"),
            statPoints = rs.getInt("stat_points"),
            allocations =
              PlayerData.deserializeAllocations(rs.getString("allocations")),
            equippedSkills =
              PlayerData.deserializeEquippedSkills(
                rs.getString("equipped_skills") ?: "",
              ),
            unlockedSkills =
              PlayerData.deserializeUnlockedSkills(
                rs.getString("unlocked_skills") ?: "",
              ),
          )
        } else {
          PlayerData(uuid)
        }
      }
    }
  }
}
