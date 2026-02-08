package net.chikina.minecraft.dungeon.enemy

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.database.repository.SpawnPointRepository
import net.chikina.minecraft.dungeon.util.Log
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class EnemySpawner(
  private val plugin: Dungeon,
  private val repository: SpawnPointRepository,
  private val enemyFactory: EnemyFactory = DefaultEnemyFactory(),
) {
  private val spawnPoints = mutableListOf<SpawnPoint>()
  private val activeEnemies = mutableMapOf<UUID, DungeonEnemy>()

  private var initialized = false

  fun initialize() {
    if (!initialized) {
      cleanupOrphans()
      loadSpawnPoints()
      initialized = true
    }
  }

  fun checkSpawns() {
    val players = Bukkit.getOnlinePlayers()

    for (spawnPoint in spawnPoints) {
      if (spawnPoint.isSpawned()) {
        val enemy = spawnPoint.activeEnemy!!

        if (enemy.isDead) {
          spawnPoint.activeEnemy = null
          spawnPoint.nextSpawnTime = System.currentTimeMillis() + (enemy.respawnTime * 50)
          continue
        }

        runCatching {
          if (enemy.location.distance(spawnPoint.location) > LEASH_RADIUS) {
            enemy.entity?.teleport(spawnPoint.location)
          }
        }

        val isPlayerNearby =
          players.any {
            it.world == spawnPoint.location.world &&
              it.location.distance(spawnPoint.location) <= DESPAWN_RADIUS
          }

        if (!isPlayerNearby) {
          enemy.despawn()
          spawnPoint.activeEnemy = null
          continue
        }

        continue
      }

      if (System.currentTimeMillis() < spawnPoint.nextSpawnTime) {
        continue
      }

      val playerNear =
        players.any {
          it.world == spawnPoint.location.world &&
            it.location.distance(spawnPoint.location) <= SPAWN_RADIUS
        }

      if (playerNear) {
        spawnEnemy(spawnPoint)
      }
    }
  }

  fun tickActiveEnemies() {
    for (enemy in activeEnemies.values) {
      if (enemy.isDead || enemy.entity == null || !enemy.entity!!.isValid) continue
      enemy.tick()
    }
  }

  private fun loadSpawnPoints() {
    spawnPoints.addAll(repository.loadAll())
    Log.info("Loaded ${spawnPoints.size} spawn points from database.")
  }

  private fun spawnEnemy(spawnPoint: SpawnPoint) {
    val enemy = enemyFactory.create(spawnPoint.type)
    enemy.spawn(spawnPoint.location)
    spawnPoint.activeEnemy = enemy
    enemy.entity?.uniqueId?.let { activeEnemies[it] = enemy }
  }

  fun addSpawnPoint(location: Location, type: String) {
    val point = SpawnPoint(location = location, type = type)
    val saved = repository.save(point)
    spawnPoints.add(saved)
  }

  fun getEnemy(entityId: UUID): DungeonEnemy? = activeEnemies[entityId]

  fun registerOneTimeEnemy(enemy: DungeonEnemy) {
    enemy.entity?.uniqueId?.let {
      activeEnemies[it] = enemy
      Log.info("Registered one-time enemy: ${enemy.name} ($it)")
    }
  }

  fun clearAllSpawnPoints() {
    var despawnCount = 0
    for (enemy in activeEnemies.values) {
      enemy.despawn()
      despawnCount++
    }
    activeEnemies.clear()

    val pointCount = spawnPoints.size
    spawnPoints.clear()

    repository.clear()

    Log.info("Cleared $pointCount spawn points and despawned $despawnCount enemies.")
  }

  fun despawnAllEnemies() {
    var count = 0
    for (enemy in activeEnemies.values) {
      enemy.despawn()
      count++
    }
    activeEnemies.clear()

    for (point in spawnPoints) {
      point.activeEnemy = null
    }

    cleanupOrphans()

    Log.info("Despawned $count active dungeon enemies.")
  }

  private fun cleanupOrphans() {
    var count = 0
    val key = NamespacedKey(plugin, "dungeon_enemy")
    for (world in Bukkit.getWorlds()) {
      for (entity in world.entities) {
        if (entity.persistentDataContainer.has(key, PersistentDataType.BYTE)) {
          entity.remove()
          count++
        }
      }
    }
    if (count > 0) {
      Log.info("Cleaned up $count orphaned Dungeon enemies.")
    }
  }

  companion object {
    private const val SPAWN_RADIUS = 15.0
    private const val LEASH_RADIUS = 8.0
    private const val DESPAWN_RADIUS = 25.0
  }
}
