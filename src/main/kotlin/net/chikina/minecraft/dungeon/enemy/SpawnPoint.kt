package net.chikina.minecraft.dungeon.enemy

import org.bukkit.Location

data class SpawnPoint(val id: Int? = null, val location: Location, val type: String) {
    var activeEnemy: DungeonEnemy? = null
    var nextSpawnTime: Long = 0

    fun isSpawned(): Boolean = activeEnemy != null && !activeEnemy!!.isDead
}
