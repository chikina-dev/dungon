package net.chikina.minecraft.dungeon.player

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.database.repository.PlayerRepository
import net.chikina.minecraft.dungeon.ui.MenuUI
import net.chikina.minecraft.dungeon.ui.Sidebar
import net.chikina.minecraft.dungeon.util.DevToggles
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack

class PlayerListener(
        private val plugin: Dungeon,
        private val playerManager: PlayerManager,
        private val playerRepository: PlayerRepository,
) : Listener {

    constructor(
            playerManager: PlayerManager,
            playerRepository: PlayerRepository
    ) : this(
            Dungeon.instance,
            playerManager,
            playerRepository,
    )

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val dungeonPlayer = playerManager.getPlayer(event.player)

        val loadedData =
                try {
                    playerRepository.load(event.player.uniqueId)
                } catch (e: Exception) {
                    Messenger.error(event.player, "データの読み込みに失敗しました！空のプロファイルを使用します。")
                    e.printStackTrace()
                    PlayerData(event.player.uniqueId)
                }
        dungeonPlayer.playerData = loadedData

        dungeonPlayer.currentHp = 20.0
        dungeonPlayer.updateStats()
        dungeonPlayer.currentMana = dungeonPlayer.stats.maxMana

        if (DevToggles.unlockTestSkillsOnFirstJoin(plugin.config) &&
                        dungeonPlayer.playerData.unlockedSkills.isEmpty()
        ) {
            dungeonPlayer.playerData.unlockedSkills.addAll(
                    listOf(
                            "test_heal",
                            "test_fireball",
                            "heavy_fireball",
                            "wind_gust",
                            "chain_lightning",
                            "meteor",
                            "ice_spear",
                    )
            )
        }

        giveMenuItem(event.player)

        dungeonPlayer.currentHp = dungeonPlayer.stats.hp
        dungeonPlayer.heal(0.0)

        Sidebar.update(event.player)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val dungeonPlayer = playerManager.getPlayer(player)
        dungeonPlayer.onDeath(null)
        val oldAcc = dungeonPlayer.playerData.accumulatedRunes

        val salvaged = dungeonPlayer.extractRunes(0.8)

        if (salvaged > 0) {
            Messenger.error(player, "死亡しました。ルーンの80% ($salvaged) を回収しました。(喪失: ${oldAcc - salvaged})")
        } else {
            Messenger.error(player, "死亡しました。")
        }

        event.keepInventory = true
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        giveMenuItem(event.player)

        val dungeonPlayer = playerManager.getPlayer(event.player)
        dungeonPlayer.player = event.player

        DungeonTask.runSync { dungeonPlayer.onRespawn() }

        dungeonPlayer.currentHp = 20.0
        dungeonPlayer.updateStats()

        dungeonPlayer.currentHp = dungeonPlayer.stats.hp
        dungeonPlayer.heal(0.0)

        Sidebar.update(event.player)
    }

    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent) {
        val dungeonPlayer = playerManager.getPlayer(event.player)
        DungeonTask.runSync { dungeonPlayer.updateStats() }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val dungeonPlayer = playerManager.getPlayer(player)

        try {
            playerRepository.save(dungeonPlayer.playerData)
        } catch (e: Exception) {
            Messenger.error(dungeonPlayer.player, "データの保存に失敗しました！")
            e.printStackTrace()
        }

        playerManager.removePlayer(player.uniqueId)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.slot == 8 && event.clickedInventory?.type == InventoryType.PLAYER) {
            val item = event.currentItem
            if (item != null && item.type == Material.NETHER_STAR) {
                event.isCancelled = true
                event.whoClicked.openInventory(MenuUI(event.whoClicked as Player).inventory)
            }
        }
        if (event.clickedInventory?.type == InventoryType.PLAYER && event.hotbarButton == 8) {
            event.isCancelled = true
        }
    }

    private fun giveMenuItem(player: Player) {
        val inventory = player.inventory
        val netherStar = ItemStack(Material.NETHER_STAR)
        val meta = netherStar.itemMeta
        meta.displayName(Component.text("§eメニュー §7(右クリック)"))
        netherStar.itemMeta = meta
        inventory.setItem(8, netherStar)
    }
}
