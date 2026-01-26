package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.SkillRegistry
import net.chikina.minecraft.dungeon.util.PluginKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SkillUnlockUI(private val player: Player) :
        DungeonUI(
                Component.text("スキル解放", NamedTextColor.DARK_AQUA),
                54,
                DungeonUIOptions(
                        clickPolicy = ClickPolicy.ITEM_INPUT,
                        inputSlots = setOf(11, 12, 13, 20, 21, 22, 29, 30, 31),
                        allowBottomInventory = true,
                        enableBackButton = false,
                ),
        ) {

    private val inputSlots = listOf(11, 12, 13, 20, 21, 22, 29, 30, 31)
    private val previewSlot = 24
    private val unlockButtonSlot = 25
    private val backButtonSlot = 49

    init {
        setupMenu()
    }

    private fun setupMenu() {
        val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val meta = glass.itemMeta
        meta.displayName(Component.empty())
        glass.itemMeta = meta
        fillBorder(glass)

        for (i in 0 until size) {
            if (!inputSlots.contains(i) &&
                            i != unlockButtonSlot &&
                            i != backButtonSlot &&
                            i != previewSlot
            ) {
                inventory.setItem(i, glass)
            }
        }

        updateUnlockButton()

        val back = ItemStack(Material.ARROW)
        val backMeta = back.itemMeta
        backMeta.displayName(Component.text("戻る", NamedTextColor.RED))
        back.itemMeta = backMeta

        addButtonAt(backButtonSlot, back) { event ->
            event.isCancelled = true
            val prev = getPreviousUI()
            if (prev != null) {
                prev.open(player, null)
            } else {
                player.closeInventory()
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        for (slot in inputSlots) {
            val item = inventory.getItem(slot) ?: continue
            if (item.type == Material.AIR) continue

            inventory.setItem(slot, null)

            val overflow = player.inventory.addItem(item)
            if (overflow.isNotEmpty()) {
                overflow.values.forEach { player.world.dropItemNaturally(player.location, it) }
            }
        }

        inventory.setItem(previewSlot, null)
    }

    override fun onClick(event: InventoryClickEvent) {
        super.onClick(event)
        updateUnlockButton()
    }

    private fun updateUnlockButton() {
        val result = evaluateUnlockability()

        val (btnMaterial, title, lore) =
                if (result.type == Unlockability.Type.UNLOCKABLE) {
                    Triple(
                            Material.LIME_DYE,
                            Component.text("解放する", NamedTextColor.GREEN),
                            listOf(Component.text("クリックしてスキルを解放", NamedTextColor.GRAY)),
                    )
                } else {
                    val reasonLine = result.message.ifBlank { "解放条件を満たしていません" }
                    Triple(
                            Material.GRAY_DYE,
                            Component.text("解放不可", NamedTextColor.RED),
                            listOf(Component.text(reasonLine, NamedTextColor.RED)),
                    )
                }

        val btn = ItemStack(btnMaterial)
        val meta = btn.itemMeta
        meta.displayName(title)
        meta.lore(lore)
        btn.itemMeta = meta

        addButtonAt(unlockButtonSlot, btn) { event ->
            event.isCancelled = true

            val latest = evaluateUnlockability()
            if (latest.type == Unlockability.Type.UNLOCKABLE) {
                attemptUnlock()
                updateUnlockButton()
            } else {
                player.sendMessage(Component.text(latest.message, NamedTextColor.RED))
                player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            }
        }

        updatePreview(result)
    }

    private fun updatePreview(result: Unlockability) {
        if (result.type != Unlockability.Type.UNLOCKABLE || result.skill == null) {
            val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            val gMeta = glass.itemMeta
            gMeta.displayName(Component.empty())
            glass.itemMeta = gMeta
            inventory.setItem(previewSlot, glass)
            return
        }

        val preview = result.skill.icon.clone()
        val pMeta = preview.itemMeta
        pMeta.displayName(Component.text("解放されるスキル: ${result.skill.name}", NamedTextColor.GOLD))
        preview.itemMeta = pMeta
        inventory.setItem(previewSlot, preview)
    }

    private data class Unlockability(
            val type: Type,
            val message: String,
            val skill: Skill? = null,
    ) {
        enum class Type {
            UNLOCKABLE,
            NOT_ENOUGH_ITEMS,
            DIFFERENT_ITEMS,
            NOT_A_SKILL_MATERIAL,
            ALREADY_UNLOCKED
        }
    }

    private fun evaluateUnlockability(): Unlockability {
        var firstItem: ItemStack? = null
        var allSame = true
        var count = 0

        for (slot in inputSlots) {
            val item = inventory.getItem(slot)
            if (item != null && item.type != Material.AIR) {
                count++
                if (firstItem == null) {
                    firstItem = item
                } else if (!isSameGameItem(firstItem, item)) {
                    allSame = false
                }
            }
        }

        if (count < 9) {
            return Unlockability(
                    Unlockability.Type.NOT_ENOUGH_ITEMS,
                    "9つ配置してください (${count}/9)",
            )
        }
        if (!allSame || firstItem == null) {
            return Unlockability(
                    Unlockability.Type.DIFFERENT_ITEMS,
                    "全て同じ素材である必要があります",
            )
        }

        val meta =
                firstItem.itemMeta
                        ?: return Unlockability(
                                Unlockability.Type.NOT_A_SKILL_MATERIAL,
                                "対応していないアイテムです"
                        )

        val materialName =
                meta.persistentDataContainer.get(PluginKeys.ITEM_ID, PersistentDataType.STRING)
                        ?: return Unlockability(
                                Unlockability.Type.NOT_A_SKILL_MATERIAL,
                                "対応していないアイテムです"
                        )

        val skill =
                SkillRegistry.getAll().find { it.unlockMaterial?.name == materialName }
                        ?: return Unlockability(
                                Unlockability.Type.NOT_A_SKILL_MATERIAL,
                                "対応していないアイテムです"
                        )

        val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
        if (dungeonPlayer.playerData.unlockedSkills.contains(skill.id)) {
            return Unlockability(Unlockability.Type.ALREADY_UNLOCKED, "既に習得済みのスキルです", skill)
        }

        return Unlockability(Unlockability.Type.UNLOCKABLE, "", skill)
    }

    private fun checkUnlockable(): String? {
        val r = evaluateUnlockability()
        return if (r.type == Unlockability.Type.UNLOCKABLE) null else r.message
    }

    private fun attemptUnlock() {
        val result = evaluateUnlockability()
        if (result.type != Unlockability.Type.UNLOCKABLE || result.skill == null) {
            player.sendMessage(Component.text(result.message, NamedTextColor.RED))
            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        val skill = result.skill

        val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
        dungeonPlayer.playerData.unlockedSkills.add(skill.id)
        player.sendMessage(Component.text("スキル「${skill.name}」を解放しました！", NamedTextColor.GREEN))
        player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)

        for (slot in inputSlots) {
            inventory.setItem(slot, null)
        }

        updateUnlockButton()
    }

    private fun isSameGameItem(i1: ItemStack, i2: ItemStack): Boolean {
        val id1 =
                i1.itemMeta?.persistentDataContainer?.get(
                        PluginKeys.ITEM_ID,
                        PersistentDataType.STRING
                )
        val id2 =
                i2.itemMeta?.persistentDataContainer?.get(
                        PluginKeys.ITEM_ID,
                        PersistentDataType.STRING
                )

        if (id1 != null && id2 != null) return id1 == id2

        return i1.isSimilar(i2)
    }
}
