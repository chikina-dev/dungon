package net.chikina.minecraft.dungeon.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

abstract class DungeonUI(
        val title: Component,
        val size: Int = 54,
        private val options: DungeonUIOptions = DungeonUIOptions(),
) : InventoryHolder {

    enum class ClickPolicy {
        BUTTONS_ONLY,
        ITEM_INPUT,
        CUSTOM,
    }

    data class DungeonUIOptions(
            val oneBasedCoordinates: Boolean = true,
            val defaultBorderItem: ItemStack? = null,
            val treatAirAsEmpty: Boolean = true,
            val clickPolicy: ClickPolicy = ClickPolicy.BUTTONS_ONLY,
            val inputSlots: Set<Int> = emptySet(),
            val allowBottomInventory: Boolean = true,
            val enableBackButton: Boolean = true,
            val backButtonPosition: Pair<Int, Int> = 6 to 5,
    )

    private val inv: Inventory = Bukkit.createInventory(this, size, title)

    private var previousUI: DungeonUI? = null

    private val buttonHandlers = mutableMapOf<Int, (InventoryClickEvent) -> Unit>()

    init {
        options.defaultBorderItem?.let { fillBorder(it) }
        if (options.enableBackButton) {
            installBackButtonIfPossible()
        }
    }

    override fun getInventory(): Inventory = inv

    open fun onClose(event: InventoryCloseEvent) {}

    fun open(player: Player, previous: DungeonUI?) {
        previousUI = previous
        player.openInventory(inv)
    }

    open fun onClick(event: InventoryClickEvent) {
        when (options.clickPolicy) {
            ClickPolicy.CUSTOM -> return
            ClickPolicy.BUTTONS_ONLY -> {
                if (event.clickedInventory != event.view.topInventory) return
                event.isCancelled = true
                buttonHandlers[event.slot]?.invoke(event)
            }
            ClickPolicy.ITEM_INPUT -> {
                if (event.clickedInventory == event.view.bottomInventory) {
                    event.isCancelled = !options.allowBottomInventory
                    return
                }

                if (event.clickedInventory != event.view.topInventory) return

                val handler = buttonHandlers[event.slot]
                if (handler != null) {
                    event.isCancelled = true
                    handler.invoke(event)
                    return
                }

                event.isCancelled = event.slot !in options.inputSlots
            }
        }
    }

    protected var cursorIndex: Int = 0

    protected fun toIndex(y: Int, x: Int): Int {
        val (yy, xx) = if (options.oneBasedCoordinates) (y - 1) to (x - 1) else y to x
        return yy * 9 + xx
    }

    protected fun setItem(y: Int, x: Int, item: ItemStack) {
        val index = toIndex(y, x)
        if (index in 0 until size) {
            inv.setItem(index, item)
        }
    }

    protected fun addButton(
            y: Int,
            x: Int,
            icon: ItemStack,
            handler: (InventoryClickEvent) -> Unit
    ) {
        val index = toIndex(y, x)
        if (index !in 0 until size) return
        inv.setItem(index, icon)
        buttonHandlers[index] = handler
    }

    protected fun addButtonAt(slot: Int, icon: ItemStack, handler: (InventoryClickEvent) -> Unit) {
        if (slot !in 0 until size) return
        inv.setItem(slot, icon)
        buttonHandlers[slot] = handler
    }

    protected fun bindButton(y: Int, x: Int, handler: (InventoryClickEvent) -> Unit) {
        val index = toIndex(y, x)
        if (index !in 0 until size) return
        buttonHandlers[index] = handler
    }

    protected fun clearButtons() {
        buttonHandlers.clear()
    }

    protected fun addItem(item: ItemStack) {
        while (cursorIndex < size && !isEmptySlot(cursorIndex)) {
            cursorIndex++
        }
        if (cursorIndex < size) {
            inv.setItem(cursorIndex, item)
            cursorIndex++
        }
    }

    protected fun fillBorder(item: ItemStack) {
        for (i in 0 until size) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                if (isEmptySlot(i)) {
                    inv.setItem(i, item)
                }
            }
        }
    }

    private fun isEmptySlot(index: Int): Boolean {
        val current = inv.getItem(index) ?: return true
        return options.treatAirAsEmpty && current.type == Material.AIR
    }

    private fun installBackButtonIfPossible() {
        val (y, x) = options.backButtonPosition
        val index = toIndex(y, x)
        if (index !in 0 until size) return

        val item =
                ItemStack(Material.ARROW).apply {
                    itemMeta =
                            itemMeta.apply { displayName(Component.text("戻る", NamedTextColor.RED)) }
                }

        addButton(y, x, item) { event ->
            val player = event.whoClicked as? Player ?: return@addButton
            val prev = previousUI
            if (prev != null) {
                prev.open(player, null)
            } else {
                player.closeInventory()
            }
        }
    }

    protected fun getPreviousUI(): DungeonUI? = previousUI
}
