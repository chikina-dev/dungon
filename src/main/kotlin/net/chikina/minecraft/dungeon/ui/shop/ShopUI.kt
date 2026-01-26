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

abstract class ShopUI(title: Component, size: Int = 54) :
        DungeonUI(
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
                                        cost.material.type.name.lowercase().replace("_", " ")
                                )
                missing.add("${cost.amount - count} ${name.toString()}")
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
                Component.text("購入: ", NamedTextColor.GREEN)
                        .append(product.resultItem.displayName())
        )
        player.playSound(player.location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
    }
}
