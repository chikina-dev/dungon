package net.chikina.minecraft.dungeon.map

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.Log

class ExplorationDungeonManager(
  private val plugin: Dungeon,
) {
  private val updateIntervalMs = 1000 * 60 * 60 // 1 hour

  fun start() {
    DungeonTask.runLater(100L) {
      Log.info("Performing startup forced update for exploration dungeons...")
      val dungeons = plugin.explorationDungeonRepository.findAll()
      for (dungeon in dungeons) {
        regenerate(dungeon)
      }
    }

    DungeonTask.runTimer(1200L, 1200L) { checkAndRegenerate() }
  }

  private fun checkAndRegenerate() {
    val dungeons = plugin.explorationDungeonRepository.findAll()
    val now = System.currentTimeMillis()

    for (dungeon in dungeons) {
      if (now - dungeon.lastUpdated >= updateIntervalMs) {
        Log.info(
          "Exploration dungeon '${dungeon.name}' (ID: ${dungeon.id}) expired. Regenerating...",
        )
        regenerate(dungeon)
      }
    }
  }

  fun regenerate(dungeon: ExplorationDungeon) {
    Log.info("Regenerating exploration dungeon: ${dungeon.name} (ID: ${dungeon.id})")

    val newSeed = System.currentTimeMillis() + dungeon.id // Ensure uniqueness
    val updatedDungeon = dungeon.copy(seed = newSeed, lastUpdated = System.currentTimeMillis())

    plugin.explorationDungeonRepository.update(updatedDungeon)

    val world = plugin.server.getWorld(dungeon.worldName)
    if (world != null) {
      try {
        DungeonGenerator.generate(updatedDungeon, world)
        Log.info("Successfully regenerated exploration dungeon: ${dungeon.name}")
      } catch (e: Exception) {
        Log.error("Failed to generate dungeon ${dungeon.name}", e)
      }
    } else {
      Log.warn("World ${dungeon.worldName} not found for dungeon ${dungeon.name}")
    }
  }
}
