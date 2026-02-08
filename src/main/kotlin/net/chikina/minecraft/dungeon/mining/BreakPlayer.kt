package net.chikina.minecraft.dungeon.mining

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.BlockPosition
import net.chikina.minecraft.dungeon.game.DropManager
import net.chikina.minecraft.dungeon.gathering.LootCalculator
import net.chikina.minecraft.dungeon.item.OreItem
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.Log
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class BreakPlayer(
  private val plugin: JavaPlugin,
  val dungeonPlayer: DungeonPlayer,
  private val dropManager: DropManager,
) {
  val player: Player = dungeonPlayer.player

  var currentBlockBeingBroken: Block? = null
    private set

  var miningDurationMillis: Double = 0.0
    private set
  var startMiningTime: Long = 0
    private set

  private var currentBlockStage: Int = 0

  private var currentCustomDrop: OreItem? = null
  private var resetTask: BukkitTask? = null

  fun startMining(
    block: Block,
    secondsBlockShouldTakeToBreak: Double,
    dropVanillaBlock: Boolean,
    customDrop: OreItem?,
  ) {
    resetTask?.cancel()
    resetTask = null

    resetTask = DungeonTask.runLater(10L) { stopMiningAndResetAnimation() }

    if (block != currentBlockBeingBroken) {
      startMiningNewBlock(block, secondsBlockShouldTakeToBreak)
    }

    this.currentCustomDrop = customDrop

    val currentTime = System.currentTimeMillis()
    val elapsed = currentTime - startMiningTime

    // 採掘の見た目 (0-9)
    if (miningDurationMillis <= 1.0) {
      breakBlock(dropVanillaBlock)
      return
    }

    val percentage = elapsed / miningDurationMillis
    if (percentage >= 1.0) {
      breakBlock(dropVanillaBlock)
      return
    }

    val newStage = (percentage * 10).toInt()
    if (newStage != currentBlockStage) {
      currentBlockStage = newStage
      sendBreakAnimation(currentBlockStage)
    }
  }

  fun stopMiningAndResetAnimation() {
    resetBreakAnimation()
    stopMining()
  }

  private fun startMiningNewBlock(block: Block, secondsBlockShouldTakeToBreak: Double) {
    val safeSeconds =
      if (secondsBlockShouldTakeToBreak < 0.05) 0.05 else secondsBlockShouldTakeToBreak
    miningDurationMillis = safeSeconds * 1000.0
    startMiningTime = System.currentTimeMillis()
    currentBlockStage = 0
    currentBlockBeingBroken = block
    sendBreakAnimation(0)
  }

  private fun stopMining() {
    resetTask?.cancel()
    resetTask = null
    currentBlockStage = 0
    currentBlockBeingBroken = null
    currentCustomDrop = null
  }

  private fun breakBlock(dropVanillaBlock: Boolean) {
    resetBreakAnimation()
    val block = currentBlockBeingBroken ?: return

    Bukkit.getPluginManager().callEvent(BlockBreakEvent(block, player))

    player.playSound(player, block.blockData.soundGroup.breakSound, 1f, 1f)

    val originalType = block.type
    val isDeepslate = originalType.name.contains("DEEPSLATE")
    val placeholderType = if (isDeepslate) Material.COBBLED_DEEPSLATE else Material.COBBLESTONE

    if (dropVanillaBlock) {
      handleDrops(block)
    }

    block.type = placeholderType

    DungeonTask.runLater(100L) {
      if (block.type == placeholderType) {
        block.type = originalType
      }
    }

    stopMining()
  }

  private fun handleDrops(block: Block) {
    val itemsToDrop = mutableListOf<ItemStack>()
    if (currentCustomDrop != null) {
      itemsToDrop.add(currentCustomDrop!!.itemStack)
    } else {
      itemsToDrop.addAll(block.getDrops(player.inventory.itemInMainHand))
    }

    val fortune = dungeonPlayer.miningEntity.miningStats.fortune
    val multiplier = LootCalculator.calculateDropMultiplier(fortune)

    for (item in itemsToDrop) {
      item.amount = (item.amount * multiplier).toInt()
      dropManager.dropPrivateItem(block.location.toCenterLocation(), item, player)
    }
  }

  private fun sendBreakAnimation(stage: Int) {
    val block = currentBlockBeingBroken ?: return
    val protocolManager = ProtocolLibrary.getProtocolManager()
    val blockBreakPacket =
      protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION)
    val entityId = (block.x * 31 * 31 + block.y * 31 + block.z)

    blockBreakPacket.integers.write(0, entityId)
    blockBreakPacket.blockPositionModifier.write(0, BlockPosition(block.x, block.y, block.z))
    blockBreakPacket.integers.write(1, stage)

    try {
      protocolManager.sendServerPacket(player, blockBreakPacket)
    } catch (ignored: Exception) {
      Log.error("ERROR IN PACKET!")
    }
  }

  private fun resetBreakAnimation() {
    sendBreakAnimation(-1)
  }
}
