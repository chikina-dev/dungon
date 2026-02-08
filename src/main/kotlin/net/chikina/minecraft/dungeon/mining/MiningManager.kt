package net.chikina.minecraft.dungeon.mining

import net.chikina.minecraft.dungeon.game.DropManager
import net.chikina.minecraft.dungeon.gathering.GatherableType
import net.chikina.minecraft.dungeon.gathering.RequirementVerifier
import net.chikina.minecraft.dungeon.gathering.ToolType
import net.chikina.minecraft.dungeon.player.PlayerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MiningManager(
  private val plugin: JavaPlugin,
  private val playerManager: PlayerManager,
  private val dropManager: DropManager,
) {
  private val breakPlayers = ConcurrentHashMap<UUID, BreakPlayer>()

  fun handleSwing(player: Player, targetBlock: Block) {
    val type = GatherableType.fromMaterial(targetBlock.type) ?: return
    // Ensure it is a Pickaxe type (Mining)
    if (type.toolType != ToolType.PICKAXE) return

    val dungeonPlayer = playerManager.getPlayer(player)
    dungeonPlayer.updateStats()

    val result = RequirementVerifier.verify(dungeonPlayer, type)
    if (result is RequirementVerifier.Result.Failure) {
      player.sendActionBar(Component.text(result.reason, NamedTextColor.RED))
      return
    }

    val finalSpeed = dungeonPlayer.miningEntity.miningStats.speed
    val effectiveSpeed = if (finalSpeed <= 0) 1 else finalSpeed
    val duration = (type.hardness * 100.0) / effectiveSpeed

    val breakPlayer =
      breakPlayers.computeIfAbsent(player.uniqueId) { _ ->
        BreakPlayer(plugin, dungeonPlayer, dropManager)
      }

    breakPlayer.startMining(targetBlock, duration, true, type.customDrop)
  }

  fun handleBlockBreak(block: Block): Boolean {
    val type = GatherableType.fromMaterial(block.type) ?: return false
    return type.toolType == ToolType.PICKAXE
  }

  fun removePlayer(uuid: UUID) {
    breakPlayers.remove(uuid)?.stopMiningAndResetAnimation()
  }

  fun getMiningInfo(player: Player): MiningInfo? {
    val breakPlayer = breakPlayers[player.uniqueId] ?: return null
    if (breakPlayer.currentBlockBeingBroken == null) return null

    return MiningInfo(breakPlayer.startMiningTime, breakPlayer.miningDurationMillis)
  }

  data class MiningInfo(
    val startTime: Long,
    val durationMillis: Double,
  )
}
