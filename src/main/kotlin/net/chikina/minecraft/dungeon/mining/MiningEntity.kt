package net.chikina.minecraft.dungeon.mining

import net.chikina.minecraft.dungeon.stats.MiningStats

interface MiningEntity {
    val miningStats: MiningStats
    fun updateStats()
}
