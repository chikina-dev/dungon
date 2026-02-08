package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.stats.StatType
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class StatAllocationUI(
  private val player: Player,
) : DungeonUI(
    Component.text("ステータス割り振り", NamedTextColor.DARK_AQUA),
    54,
    DungeonUIOptions(
      defaultBorderItem = UIStyles.borderGlass(),
      enableBackButton = true,
    ),
  ) {
  private val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)

  init {
    updateUI()
  }

  private fun updateUI() {
    inventory.clear()
    fillBorder(UIStyles.borderGlass())

    val data = dungeonPlayer.playerData
    val cost = data.level * 100L

    val infoItem = ItemStack(Material.NETHER_STAR)
    val infoMeta = infoItem.itemMeta
    infoMeta.displayName(Component.text("レベル: ${data.level}", NamedTextColor.YELLOW))
    infoMeta.lore(
      listOf(
        Component.text("ステータスポイント: ${data.statPoints}", NamedTextColor.AQUA),
        Component.empty(),
        Component.text("レベルアップ費用: $cost ルーン", NamedTextColor.GOLD),
        Component.text(">> クリックでレベルアップ <<", NamedTextColor.GREEN),
      ),
    )
    infoItem.itemMeta = infoMeta

    addButton(1, 5, infoItem) { _ ->
      val current = dungeonPlayer.playerData
      val currentCost = current.level * 100L

      if (current.runes >= currentCost) {
        current.runes -= currentCost
        current.level++
        current.statPoints += 5
        Messenger.success(player, "レベルアップ！ レベル ${current.level} になりました。")
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        Sidebar.update(player)
        updateUI()
      } else {
        Messenger.error(player, "ルーンが足りません！")
      }
    }

    addStatButton(3, 2, StatType.VITALITY, Material.RED_DYE, "体力 (HP)")
    addStatButton(3, 4, StatType.ENDURANCE, Material.IRON_CHESTPLATE, "耐久 (Def)")
    addStatButton(3, 6, StatType.MAGIC, Material.LAPIS_LAZULI, "魔力 (MP)")
    addStatButton(3, 8, StatType.STRENGTH, Material.IRON_SWORD, "筋力 (Atk)")

    addStatButton(4, 2, StatType.INTELLIGENCE, Material.BOOK, "知力 (Mag Atk)")
    addStatButton(4, 4, StatType.DEXTERITY, Material.FEATHER, "器用 (Crit Dmg)")
    addStatButton(4, 6, StatType.FAITH, Material.BLAZE_POWDER, "信仰 (Elem Atk)")
    addStatButton(4, 8, StatType.FATE, Material.GOLD_NUGGET, "運命 (Luck/Crit)")
  }

  private fun addStatButton(
    row: Int,
    col: Int,
    type: StatType,
    material: Material,
    name: String,
  ) {
    val current = dungeonPlayer.playerData.allocations.getOrDefault(type, 0)
    val item = ItemStack(material)
    val meta = item.itemMeta
    meta.displayName(Component.text(name, NamedTextColor.GOLD))

    val description =
      when (type) {
        StatType.VITALITY -> "HP +5"
        StatType.ENDURANCE -> "防御力 +1"
        StatType.MAGIC -> "MP (未実装)"
        StatType.STRENGTH -> "物理攻撃 +2"
        StatType.INTELLIGENCE -> "魔法攻撃 +1%"
        StatType.DEXTERITY -> "クリティカルダメージ +0.8%, 率 +0.05%"
        StatType.FAITH -> "全属性攻撃 +0.5%"
        StatType.FATE -> "ドロップ率・ルーン +0.5%"
      }

    meta.lore(
      listOf(
        Component.text("現在値: $current", NamedTextColor.WHITE),
        Component.text(description, NamedTextColor.GRAY),
        Component.empty(),
        Component.text("クリックで +1", NamedTextColor.YELLOW),
      ),
    )
    item.itemMeta = meta

    addButton(row, col, item) { _ ->
      val data = dungeonPlayer.playerData
      if (data.statPoints > 0) {
        data.statPoints--
        data.allocations[type] = data.allocations.getOrDefault(type, 0) + 1

        dungeonPlayer.updateStats()
        val newVal = data.allocations.getOrDefault(type, 0)
        Messenger.success(player, "${getStatName(type)} を $newVal に増やしました！")
        Sidebar.update(player)
        updateUI()
      } else {
        Messenger.error(player, "ステータスポイントが足りません！")
      }
    }
  }

  private fun getStatName(type: StatType): String = when (type) {
    StatType.VITALITY -> "体力"
    StatType.ENDURANCE -> "耐久"
    StatType.MAGIC -> "魔力"
    StatType.STRENGTH -> "筋力"
    StatType.INTELLIGENCE -> "知力"
    StatType.DEXTERITY -> "器用"
    StatType.FAITH -> "信仰"
    StatType.FATE -> "運命"
  }
}
