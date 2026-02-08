package net.chikina.minecraft.dungeon.ui.shop

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.ui.Sidebar
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

class ShopSellListener : Listener {
  @EventHandler
  fun onInventoryClick(event: InventoryClickEvent) {
    val holder = event.view.topInventory.holder
    if (holder !is ShopUI) return

    val player = event.whoClicked as? Player ?: return

    val before = snapshot(event.view.topInventory)

    DungeonTask.run { sellDiff(player, holder, event.view.topInventory, before) }
  }

  @EventHandler
  fun onInventoryDrag(event: InventoryDragEvent) {
    val holder = event.view.topInventory.holder
    if (holder !is ShopUI) return

    val player = event.whoClicked as? Player ?: return

    val before = snapshot(event.view.topInventory)

    DungeonTask.run { sellDiff(player, holder, event.view.topInventory, before) }
  }

  private fun snapshot(top: Inventory): Array<ItemStack?> = Array(top.size) { i -> top.getItem(i)?.clone() }

  private fun sellDiff(
    player: Player,
    shop: ShopUI,
    top: Inventory,
    before: Array<ItemStack?>,
  ) {
    val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)

    var total = 0L

    for (slot in 0 until top.size) {
      val prev = before.getOrNull(slot)
      val current = top.getItem(slot)

      if (sameStack(prev, current)) continue

      if (current == null || current.type == Material.AIR) continue

      val unitPrice = getSellPriceFromShopTable(shop, current)
      if (unitPrice <= 0L) {
        continue
      }

      val sold = unitPrice * current.amount
      total += sold

      dungeonPlayer.addSoldItem(current.clone(), sold)
      top.setItem(slot, null)
    }

    if (total > 0L) {
      dungeonPlayer.playerData.runes += total
      Sidebar.update(player)
      Messenger.send(player, Component.text("売却: +$total ルーン", NamedTextColor.AQUA))
      player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
    }
  }

  private fun sameStack(a: ItemStack?, b: ItemStack?): Boolean {
    if (a == null && b == null) return true
    if (a == null || b == null) return false
    if (!a.isSimilar(b)) return false
    return a.amount == b.amount
  }

  private fun getSellPriceFromShopTable(shop: ShopUI, item: ItemStack): Long {
    when (item.type) {
      Material.BLACK_STAINED_GLASS_PANE,
      Material.GRAY_STAINED_GLASS_PANE,
      Material.CYAN_STAINED_GLASS_PANE,
      Material.GLASS_PANE,
      Material.HOPPER,
      Material.NETHER_STAR,
      Material.AIR,
      -> return 0

      else -> Unit
    }

    val product = shop.productsForSell().firstOrNull { p -> p.resultItem.isSimilar(item) }

    if (product != null) {
      return floor(product.runeCost * 0.8).toLong()
    }

    return 1
  }
}
