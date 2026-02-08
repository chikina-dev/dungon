package net.chikina.minecraft.dungeon.ui.shop

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.ui.DungeonUI
import net.chikina.minecraft.dungeon.ui.Sidebar
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class ShopUI(
  title: Component,
  size: Int = 54,
) : DungeonUI(
    title,
    size,
    DungeonUIOptions(
      enableBackButton = true,
    ),
  ) {
  protected val products = mutableMapOf<Int, ShopProduct>()

  internal fun productsForSell(): Collection<ShopProduct> = products.values

  init {
    if (size >= 54) {
      val hopper = ItemStack(Material.HOPPER)
      val meta = hopper.itemMeta
      meta.displayName(Component.text("買い戻し", NamedTextColor.GOLD))
      meta.lore(listOf(Component.text("クリックで買い戻し一覧", NamedTextColor.YELLOW)))
      hopper.itemMeta = meta

      addButtonAt(49, hopper) { event ->
        val player = event.whoClicked as? Player ?: return@addButtonAt
        BuybackUI(player).open(player, this)
      }
    }
  }

  protected fun addProduct(product: ShopProduct) {
    addItem(product.displayItem)
    val slot = cursorIndex - 1
    products[slot] = product
    bindProductButton(slot)
  }

  protected fun addProduct(y: Int, x: Int, product: ShopProduct) {
    val slot = toIndex(y, x)
    products[slot] = product
    addButtonAt(slot, product.displayItem) { event ->
      val player = event.whoClicked as? Player ?: return@addButtonAt
      purchase(player, product)
    }
  }

  protected fun addProducts(y: Int, x: Int, productsList: Iterable<ShopProduct>) {
    var currentX = x
    var currentY = y

    for (product in productsList) {
      addProduct(currentY, currentX, product)
      currentX--
      if (currentX < 1) {
        currentX = 9
        currentY--
        if (currentY < 1) break
      }
    }
  }

  private fun bindProductButton(slot: Int) {
    val product = products[slot] ?: return
    addButtonAt(slot, inventory.getItem(slot) ?: ItemStack(Material.AIR)) { event ->
      val player = event.whoClicked as? Player ?: return@addButtonAt
      purchase(player, product)
    }
  }

  override fun open(player: Player, previous: DungeonUI?) {
    updateLore(player)
    super.open(player, previous)
  }

  private fun updateLore(player: Player) {
    val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
    val inv = player.inventory

    for ((slot, product) in products) {
      val displayItem = product.displayItem // This is the item in the inventory
      val meta = displayItem.itemMeta
      val baseLore = meta.lore() ?: mutableListOf()

      // Remove existing cost lines (heuristic: starts with "Cost:" or "コスト:")
      // Better approach: Rebuild from product.originalLore (if we had it) or just append to a
      // clean state.
      // Since we don't store original lore separately in ShopProduct easily without refactor,
      // we will assume costs are appended at the end and we can just clear and rebuild or use
      // a marker.
      // For now, let's just clear specific lines or simpler: ShopProduct should hold the
      // "clean" display item.
      // Actually, we can use product.displayItem as the "template" but we need to reset it
      // every time.
      // But product.displayItem IS the item in the slot if we don't clone it.
      // Let's rely on the fact that `setupShop` creates the `product` with a `displayItem`.
      // We should treat `product.displayItem` as the immutable template and clone it for the
      // inventory view?
      // Existing code: `addItem(product.displayItem)`.
      // If we modify the item in the inventory, `product.displayItem` (the template) remains
      // valid?
      // Actually `products` map holds the product. `product.displayItem` is the OBJ.
      // If we modify `inventory.getItem(slot)`, we don't touch `product.displayItem` unless
      // they are same ref.
      // `addItem` usually clones or just sets.
      // Let's act on `inventory.getItem(slot)`.

      val itemInSlot = inventory.getItem(slot) ?: continue
      val slotMeta = itemInSlot.itemMeta
      // We need the BASE lore (without previous costs).
      // A simple way regarding current architecture:
      // product.displayItem has the "Static" lore (name, description).
      // We clone it, add dynamic costs, and set it to slot.

      val newStack = product.displayItem.clone()
      val newMeta = newStack.itemMeta
      val lore = newMeta.lore() ?: mutableListOf()

      // Calculate costs
      val runeCost = product.runeCost
      val hasRunes = dungeonPlayer.playerData.runes >= runeCost
      val runeColor = if (hasRunes) NamedTextColor.GREEN else NamedTextColor.RED

      if (runeCost > 0) {
        lore.add(Component.text("Cost: $runeCost Runes", runeColor))
      }

      for (cost in product.costs) {
        var count = 0
        for (item in inv.contents) {
          if (item != null && item.isSimilar(cost.material)) {
            count += item.amount
          }
        }

        val hasItem = count >= cost.amount
        val color = if (hasItem) NamedTextColor.GREEN else NamedTextColor.RED

        val name =
          cost.material.itemMeta?.displayName()
            ?: Component.text(
              cost.material.type.name
                .lowercase()
                .replace("_", " "),
            )

        lore.add(Component.text("Cost: ${cost.amount} ", color).append(name))
      }

      newMeta.lore(lore)
      newStack.itemMeta = newMeta
      inventory.setItem(slot, newStack)
    }
  }

  private fun purchase(player: Player, product: ShopProduct) {
    val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
    val inv = player.inventory

    val missing = mutableListOf<String>()

    if (dungeonPlayer.playerData.runes < product.runeCost) {
      missing.add("${product.runeCost - dungeonPlayer.playerData.runes} Runes")
    }

    for (cost in product.costs) {
      var count = 0
      for (item in inv.contents) {
        if (item != null && item.isSimilar(cost.material)) {
          count += item.amount
        }
      }
      if (count < cost.amount) {
        val name =
          cost.material.itemMeta?.displayName()
            ?: Component.text(
              cost.material.type.name
                .lowercase()
                .replace("_", " "),
            )
        // Convert Component to String for message
        val nameStr =
          net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
            .plainText()
            .serialize(name)
        missing.add("${cost.amount - count} $nameStr")
      }
    }

    if (missing.isNotEmpty()) {
      Messenger.error(player, "素材が足りません: ${missing.joinToString(", ")}")
      player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
      return
    }

    if (product.runeCost > 0) {
      dungeonPlayer.playerData.runes -= product.runeCost
      Sidebar.update(player)
    }

    for (cost in product.costs) {
      var remaining = cost.amount
      for (i in 0 until inv.size) {
        val item = inv.getItem(i)
        if (item != null && item.isSimilar(cost.material)) {
          val toRemove = kotlin.math.min(item.amount, remaining)
          item.amount -= toRemove
          remaining -= toRemove
          if (item.amount <= 0) inv.setItem(i, null) else inv.setItem(i, item)
          if (remaining <= 0) break
        }
      }
    }

    player.inventory.addItem(product.resultItem)
    Messenger.send(
      player,
      Component
        .text("購入: ", NamedTextColor.GREEN)
        .append(product.resultItem.displayName()),
    )
    player.playSound(player.location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

    // Refresh UI to update colors
    updateLore(player)
  }
}
