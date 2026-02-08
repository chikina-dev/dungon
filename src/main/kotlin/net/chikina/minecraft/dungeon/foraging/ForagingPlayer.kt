package net.chikina.minecraft.dungeon.foraging

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
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class ForagingPlayer(
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
  private var onBreakCallback: ((Block) -> Unit)? = null

  fun startForaging(
    block: Block,
    secondsBlockShouldTakeToBreak: Double,
    customDrop: OreItem?,
    onBreak: (Block) -> Unit,
  ) {
    resetTask?.cancel()
    resetTask = null

    // Reset animation if we haven't received an update in a while (e.g. 10 ticks)
    resetTask = DungeonTask.runLater(10L) { stopForagingAndResetAnimation() }

    if (block != currentBlockBeingBroken) {
      startForagingNewBlock(block, secondsBlockShouldTakeToBreak)
    }

    currentBlockBeingBroken = block
    this.currentCustomDrop = customDrop
    this.onBreakCallback = onBreak

    val currentTime = System.currentTimeMillis()
    val elapsed = currentTime - startMiningTime

    // Immediate break
    if (miningDurationMillis <= 1.0) {
      breakBlock()
      return
    }

    val percentage = elapsed / miningDurationMillis
    if (percentage >= 1.0) {
      breakBlock()
      return
    }

    // Animation Stage (0-9)
    val newStage = (percentage * 10).toInt()
    if (newStage != currentBlockStage) {
      currentBlockStage = newStage
      sendBreakAnimation(currentBlockStage)
    }
  }

  private fun tickMining() {
    val block = currentBlockBeingBroken ?: return

    val currentTime = System.currentTimeMillis()
    val elapsed = currentTime - startMiningTime
    val percentage = elapsed / miningDurationMillis

    if (percentage >= 1.0) {
      breakBlock()
      return
    }

    // Animation Stage (0-9)
    val newStage = (percentage * 10).toInt()
    if (newStage != currentBlockStage) {
      currentBlockStage = newStage
      sendBreakAnimation(currentBlockStage)
    }
  }

  fun stopForagingAndResetAnimation() {
    resetBreakAnimation()
    stopForaging()
  }

  // NOTE: startForagingNewBlock is removed as it is merged into startForaging

  private fun startForagingNewBlock(block: Block, secondsBlockShouldTakeToBreak: Double) {
    val safeSeconds =
      if (secondsBlockShouldTakeToBreak < 0.05) 0.05 else secondsBlockShouldTakeToBreak
    miningDurationMillis = safeSeconds * 1000.0
    startMiningTime = System.currentTimeMillis()
    currentBlockStage = 0
    currentBlockBeingBroken = block
    sendBreakAnimation(0)
  }

  private fun stopForaging() {
    resetTask?.cancel()
    resetTask = null
    currentBlockStage = -1
    currentBlockBeingBroken = null
    currentCustomDrop = null
    onBreakCallback = null
  }

  private fun breakBlock() {
    resetBreakAnimation()
    val block = currentBlockBeingBroken ?: return

    // Fire break event
    Bukkit.getPluginManager().callEvent(BlockBreakEvent(block, player))

    // Sound
    player.playSound(player, block.blockData.soundGroup.breakSound, 1f, 1f)

    // Callback (Tree logic)
    onBreakCallback?.invoke(block)

    // Drops
    handleDrops(block)

    // Physical Break (usually handled by callback, but ensure AIR if needed)
    block.type = Material.AIR

    stopForaging()
  }

  private fun handleDrops(block: Block) {
    val itemsToDrop = mutableListOf<org.bukkit.inventory.ItemStack>()
    if (currentCustomDrop != null) {
      itemsToDrop.add(currentCustomDrop!!.itemStack)
    } else {
      itemsToDrop.addAll(block.getDrops(player.inventory.itemInMainHand))
    }

    val multiplier = calculateDropMultiplier()

    for (item in itemsToDrop) {
      item.amount = (item.amount * multiplier).toInt()
      dropManager.dropPrivateItem(block.location.toCenterLocation(), item, player)
    }
  }

  private fun calculateDropMultiplier(): Int {
    val fortune = dungeonPlayer.miningEntity.miningStats.fortune
    return LootCalculator.calculateDropMultiplier(fortune)
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
