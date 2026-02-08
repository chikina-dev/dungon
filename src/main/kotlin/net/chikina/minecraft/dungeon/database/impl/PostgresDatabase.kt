package net.chikina.minecraft.dungeon.database.impl

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.chikina.minecraft.dungeon.database.Database
import org.postgresql.Driver
import java.sql.Connection

class PostgresDatabase(
  private val jdbcUrl: String,
  private val username: String,
  private val password: String,
) : Database {
  private var dataSource: HikariDataSource? = null

  override fun connect() {
    if (dataSource != null && !dataSource!!.isClosed) return

    val config = HikariConfig()
    config.jdbcUrl = jdbcUrl
    config.username = username
    config.password = password
    config.driverClassName = Driver::class.java.name
    config.maximumPoolSize = 10
    config.minimumIdle = 5
    config.connectionTimeout = 30000
    config.idleTimeout = 600000
    config.maxLifetime = 1800000

    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

    dataSource = HikariDataSource(config)

    getConnection().use { conn ->
      conn.createStatement().use { stmt ->
        stmt.execute(
          """
          CREATE TABLE IF NOT EXISTS player_data (
              uuid VARCHAR(36) PRIMARY KEY,
              runes BIGINT DEFAULT 0,
              level INT DEFAULT 1,
              stat_points INT DEFAULT 0,
              allocations TEXT DEFAULT '',
              equipped_skills TEXT DEFAULT '',
              unlocked_skills TEXT DEFAULT ''
          );
          
          ALTER TABLE player_data ADD COLUMN IF NOT EXISTS equipped_skills TEXT DEFAULT '';
          ALTER TABLE player_data ADD COLUMN IF NOT EXISTS unlocked_skills TEXT DEFAULT '';
          
          CREATE TABLE IF NOT EXISTS enemy_spawn_points (
              id SERIAL PRIMARY KEY,
              world VARCHAR(255) NOT NULL,
              x DOUBLE PRECISION NOT NULL,
              y DOUBLE PRECISION NOT NULL,
              z DOUBLE PRECISION NOT NULL,
              type VARCHAR(255) NOT NULL
          );
          
          CREATE TABLE IF NOT EXISTS exploration_dungeons (
              id SERIAL PRIMARY KEY,
              name VARCHAR(255) NOT NULL DEFAULT 'default',
              x1 INT NOT NULL,
              y1 INT NOT NULL,
              z1 INT NOT NULL,
              x2 INT NOT NULL,
              y2 INT NOT NULL,
              z2 INT NOT NULL,
              world_name VARCHAR(255) NOT NULL,
              seed BIGINT NOT NULL,
              last_updated BIGINT NOT NULL
          );
          """.trimIndent(),
        )
      }
    }
  }

  override fun disconnect() {
    dataSource?.close()
    dataSource = null
  }

  override fun getConnection(): Connection = dataSource?.connection ?: throw IllegalStateException("Database not connected")
}
