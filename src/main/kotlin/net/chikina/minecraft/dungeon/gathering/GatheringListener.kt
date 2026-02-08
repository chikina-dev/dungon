package net.chikina.minecraft.dungeon.gathering

import net.chikina.minecraft.dungeon.event.DungeonMiningSwingEvent
import net.chikina.minecraft.dungeon.foraging.ForagingManager
import net.chikina.minecraft.dungeon.foraging.TreeManager
import net.chikina.minecraft.dungeon.mining.MiningManager
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

class GatheringListener(
  private val miningManager: MiningManager,
  private val foragingManager: ForagingManager,
) : Listener {
  @EventHandler(priority = EventPriority.HIGH)
  fun onPlayerInteract(event: PlayerInteractEvent) {
    if (event.action != Action.LEFT_CLICK_BLOCK) return
    val block = event.clickedBlock ?: return
    val player = event.player
    if (player.gameMode == GameMode.CREATIVE) return

    val type = GatherableType.fromMaterial(block.type) ?: return

    when (type.toolType) {
      ToolType.PICKAXE -> {
        miningManager.handleSwing(player, block)
      }

      ToolType.AXE -> {
        foragingManager.handleSwing(player, block) { b -> TreeManager.handleBlockBreak(b) }
      }

      else -> {}
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  fun onBlockBreak(event: BlockBreakEvent) {
    if (event.player.gameMode == GameMode.CREATIVE) return
    val block = event.block

    // If it is a gatherable type, we usually cancel vanilla break in survival
    // unless it's being broken by our custom system which triggers artificial breaks.
    // However, standard left-click interact usually starts the custom mining/foraging process.
    // If the player manages to break it instantly (e.g. powerful tool in vanilla), we might want to
    // handle it.
    // But in this custom system, usually interact -> custom break logic -> artificial block break
    // event.

    // Check if it's a registered gatherable
    val type = GatherableType.fromMaterial(block.type)
    if (type != null) {
      // Cancel vanilla break for these custom blocks in survival,
      // relying on Managers to handle the logic and drops.
      // Managers call 'breakBlock' which might fire BlockBreakEvent again?
      // If Managers fire BlockBreakEvent, we need to ignore THAT event.
      // But Managers usually simulate break.

      // For now, let's keep it simple: MiningManager usually handles cancellation internally or
      // relies on adventure mode/mining fatigue to prevent vanilla breaks.
      // If we assume adventure mode/slow mining, we might not need to cancel explicitly here unless
      // the player is bypassing it.

      // Let's delegate cancellation check to Managers if needed, or just cancel here if it's a
      // valid Gathering target.
      // Existing logic often cancels in "MiningListener".

      event.isCancelled = true
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  fun onMiningSwing(event: DungeonMiningSwingEvent) {
    val player = event.player
    val block = event.block
    if (player.gameMode == GameMode.CREATIVE) return

    val type = GatherableType.fromMaterial(block.type) ?: return

    when (type.toolType) {
      ToolType.PICKAXE -> {
        miningManager.handleSwing(player, block)
      }

      ToolType.AXE -> {
        foragingManager.handleSwing(player, block) { b -> TreeManager.handleBlockBreak(b) }
      }

      else -> {}
    }
  }
}
