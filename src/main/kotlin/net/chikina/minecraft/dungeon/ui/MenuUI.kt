package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class MenuUI(private val player: Player) :
        DungeonUI(
                Component.text("メニュー", NamedTextColor.DARK_PURPLE),
                27,
                DungeonUIOptions(
                        defaultBorderItem = UIStyles.borderGlass(Material.GRAY_STAINED_GLASS_PANE)
                ),
        ) {

    private val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)

    init {
        setupMenu()
    }

    private fun setupMenu() {
        val statusItem = ItemStack(Material.BOOK)
        val sMeta = statusItem.itemMeta
        sMeta.displayName(Component.text("ステータス", NamedTextColor.GOLD))

        val stats = dungeonPlayer.stats
        val mining = dungeonPlayer.miningEntity.miningStats

        sMeta.lore(
                listOf(
                        Component.empty(),
                        Component.text("戦闘ステータス:", NamedTextColor.RED),
                        Component.text(" HP: ${stats.hp}", NamedTextColor.GRAY),
                        Component.text(
                                " 攻撃力: ${stats.attack.baseAttack} (+${stats.attack.physicalAttack}%)",
                                NamedTextColor.GRAY
                        ),
                        Component.text(
                                " 防御力: ${stats.defense.baseDefense} (+${stats.defense.physicalDefense}%)",
                                NamedTextColor.GRAY
                        ),
                        Component.text(" 会心率: ${stats.critRate}%", NamedTextColor.GRAY),
                        Component.text(" 会心ダメ: ${stats.critDamage}%", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("採掘ステータス:", NamedTextColor.AQUA),
                        Component.text(" 採掘速度: ${mining.speed}", NamedTextColor.GRAY),
                        Component.text(" 採掘運: ${mining.fortune}", NamedTextColor.GRAY)
                )
        )
        sMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        statusItem.itemMeta = sMeta
        setItem(2, 2, statusItem)

        val skillItem = ItemStack(Material.ENCHANTED_BOOK)
        val skMeta = skillItem.itemMeta
        skMeta.displayName(Component.text("スキル選択", NamedTextColor.LIGHT_PURPLE))
        skMeta.lore(listOf(Component.text("装備スキルを変更", NamedTextColor.GRAY)))
        skillItem.itemMeta = skMeta
        addButton(2, 4, skillItem) { _ -> SkillSelectionUI(player).open(player, this) }

        val statItem = ItemStack(Material.EXPERIENCE_BOTTLE)
        val meta = statItem.itemMeta
        meta.displayName(Component.text("ステータス割り振り", NamedTextColor.GREEN))
        meta.lore(listOf(Component.text("クリックして強化", NamedTextColor.GRAY)))
        statItem.itemMeta = meta
        addButton(2, 6, statItem) { _ -> StatAllocationUI(player).open(player, this) }

        if (dungeonPlayer.stats.hp > 0) {
            val spawnItem = ItemStack(Material.COMPASS)
            val spMeta = spawnItem.itemMeta
            spMeta.displayName(Component.text("拠点へ帰還", NamedTextColor.AQUA))
            spMeta.lore(
                    listOf(
                            Component.text("クリックして帰還し、", NamedTextColor.GRAY),
                            Component.text("ルーンを100%回収します。", NamedTextColor.GRAY)
                    )
            )
            spawnItem.itemMeta = spMeta

            addButton(2, 8, spawnItem) { _ ->
                player.closeInventory()

                val extracted = dungeonPlayer.extractRunes(1.0)
                if (extracted > 0) {
                    Messenger.send(
                            player,
                            Component.text("ルーンを $extracted (100%) 回収しました！", NamedTextColor.GREEN)
                    )
                }

                player.teleport(player.world.spawnLocation)
                Sidebar.update(player)
            }
        }
    }
}
