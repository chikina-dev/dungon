package net.chikina.minecraft.dungeon.mining

import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.MiningItem
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.stats.StatCalculator

class PlayerMiningEntity(
  private val dungeonPlayer: DungeonPlayer,
) : MiningEntity {
  override var miningStats: MiningStats = MiningStats()
    private set

  override fun updateStats() {
    miningStats = StatCalculator.calculateMiningStats(dungeonPlayer.playerData)

    val item = dungeonPlayer.player.inventory.itemInMainHand
    val gameItem = GameItem(item)
    val miningItem = MiningItem(gameItem.itemStack)

    miningStats.speed += miningItem.miningSpeed
    miningStats.breakingPower =
      if (miningItem.breakingPower > 0) {
        miningItem.breakingPower
      } else {
        miningStats.breakingPower
      }
    miningStats.fortune += miningItem.miningFortune
  }
}
