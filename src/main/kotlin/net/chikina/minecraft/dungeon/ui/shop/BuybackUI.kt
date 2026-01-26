package net.chikina.minecraft.dungeon.ui.shop

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.ui.DungeonUI
import net.chikina.minecraft.dungeon.ui.Sidebar
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Sound
import org.bukkit.entity.Player

class BuybackUI(private val player: Player) :
        DungeonUI(Component.text("買い戻し (Buyback)", NamedTextColor.DARK_RED), 54) {

    private val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
    private val slotMap = mutableMapOf<Int, DungeonPlayer.SoldItem>()

    init {
        updateUI()
    }

    private fun updateUI() {
        inventory.clear()
        slotMap.clear()
        clearButtons()

        val items = dungeonPlayer.soldItems

        for ((index, soldItem) in items.withIndex()) {
            if (index >= 54) break

            val displayItem = soldItem.itemStack.clone()
            val meta = displayItem.itemMeta
            val currentLore = meta.lore() ?: mutableListOf()

            currentLore.add(Component.empty())
            currentLore.add(
                    Component.text("買い戻し価格: ${soldItem.soldPrice} ルーン", NamedTextColor.YELLOW)
            )
            currentLore.add(Component.text("クリックで買い戻す", NamedTextColor.GREEN))

            meta.lore(currentLore)
            displayItem.itemMeta = meta

            slotMap[index] = soldItem

            addButtonAt(index, displayItem) { _ -> buyback(index) }
        }
    }

    private fun buyback(slot: Int) {
        val soldItem = slotMap[slot] ?: return
        val price = soldItem.soldPrice

        if (dungeonPlayer.playerData.runes >= price) {
            if (player.inventory.firstEmpty() == -1) {
                Messenger.error(player, "インベントリがいっぱいです！")
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
                return
            }

            dungeonPlayer.playerData.runes -= price
            player.inventory.addItem(soldItem.itemStack)
            dungeonPlayer.soldItems.remove(soldItem)

            Messenger.success(player, "アイテムを買い戻しました。")
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            Sidebar.update(player)

            updateUI()
        } else {
            Messenger.error(player, "ルーンが足りません！ (必要: $price)")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
        }
    }
}
