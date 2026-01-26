package net.chikina.minecraft.dungeon.mining.core

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import net.chikina.minecraft.dungeon.player.PlayerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class MiningManager(private val plugin: JavaPlugin, private val playerManager: PlayerManager) {

    private val breakPlayers = ConcurrentHashMap<UUID, BreakPlayer>()

    fun handleSwing(player: Player, targetBlock: Block) {
        val oreData = OreRegistry.getOreData(targetBlock.type) ?: return

        val dungeonPlayer = playerManager.getPlayer(player)
        dungeonPlayer.updateStats()
        val power = dungeonPlayer.miningEntity.miningStats.breakingPower
        val finalSpeed = dungeonPlayer.miningEntity.miningStats.speed

        if (power < oreData.tier) {
            player.sendActionBar(
                    Component.text("この鉱石を掘るには破壊力が ${oreData.tier} 必要です！", NamedTextColor.RED)
            )
            return
        }

        val effectiveSpeed = if (finalSpeed <= 0) 1 else finalSpeed
        val duration = (oreData.hardness * 100.0) / effectiveSpeed

        val breakPlayer =
                breakPlayers.computeIfAbsent(player.uniqueId) { _ ->
                    BreakPlayer(plugin, dungeonPlayer)
                }

        breakPlayer.startMining(targetBlock, duration, true, oreData.customDrop)
    }

    fun handleBlockBreak(block: Block): Boolean {
        return OreRegistry.isOre(block.type)
    }

    fun removePlayer(uuid: UUID) {
        breakPlayers.remove(uuid)?.stopMiningAndResetAnimation()
    }

    fun getMiningInfo(player: Player): MiningInfo? {
        val breakPlayer = breakPlayers[player.uniqueId] ?: return null
        if (breakPlayer.currentBlockBeingBroken == null) return null

        return MiningInfo(breakPlayer.startMiningTime, breakPlayer.miningDurationMillis)
    }

    data class MiningInfo(val startTime: Long, val durationMillis: Double)
}
