package net.chikina.minecraft.dungeon.foraging

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

class ForagingManager(
  private val plugin: JavaPlugin,
  private val playerManager: PlayerManager,
  private val dropManager: DropManager,
) {
  private val foragingPlayers = ConcurrentHashMap<UUID, ForagingPlayer>()

  fun handleSwing(player: Player, targetBlock: Block, onBreak: (Block) -> Unit) {
    val gatherable = GatherableType.fromMaterial(targetBlock.type) ?: return

    // Only handle logs here
    if (gatherable.toolType != ToolType.AXE) return

    val dungeonPlayer = playerManager.getPlayer(player)
    dungeonPlayer.updateStats() // Ensure stats are fresh

    val result = RequirementVerifier.verify(dungeonPlayer, gatherable)
    if (result is RequirementVerifier.Result.Failure) {
      player.sendActionBar(Component.text(result.reason, NamedTextColor.RED))
      return
    }

    // Check held item
    val mainHand = player.inventory.itemInMainHand
    val isAxe = mainHand.type.name.endsWith("_AXE")

    val finalSpeed =
      if (isAxe) {
        dungeonPlayer.miningEntity.miningStats.speed
      } else {
        100 // Default hand speed if not axe
      }
    val effectiveSpeed = if (finalSpeed <= 0) 1 else finalSpeed
    val duration = (gatherable.hardness * 100.0) / effectiveSpeed

    val foragingPlayer =
      foragingPlayers.computeIfAbsent(player.uniqueId) { _ ->
        ForagingPlayer(plugin, dungeonPlayer, dropManager)
      }

    // Start or Continue Foraging
    foragingPlayer.startForaging(targetBlock, duration, gatherable.customDrop, onBreak)
  }

  fun removePlayer(uuid: UUID) {
    foragingPlayers.remove(uuid)?.stopForagingAndResetAnimation()
  }
}
