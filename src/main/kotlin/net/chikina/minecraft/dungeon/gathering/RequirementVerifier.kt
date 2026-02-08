package net.chikina.minecraft.dungeon.gathering

import net.chikina.minecraft.dungeon.player.DungeonPlayer

object RequirementVerifier {
  sealed class Result {
    object Success : Result()

    data class Failure(
      val reason: String,
    ) : Result()
  }

  fun verify(dungeonPlayer: DungeonPlayer, gatherable: GatherableType): Result {
    val power = dungeonPlayer.miningEntity.miningStats.breakingPower
    if (power < gatherable.tier) {
      return Result.Failure("この資源を採集するには力 ${gatherable.tier} 必要です！")
    }
    return Result.Success
  }
}
