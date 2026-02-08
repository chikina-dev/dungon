package net.chikina.minecraft.dungeon.ui

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.SkillRegistry
import net.chikina.minecraft.dungeon.combat.skill.SkillSlot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class SkillSelectionUI(
  private val player: Player,
) : DungeonUI(Component.text("スキル選択", NamedTextColor.DARK_PURPLE), 54) {
  private val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
  private val playerData = dungeonPlayer.playerData

  private var page = 0
  private val pageSize = 21

  init {
    setupMenu()
  }

  private fun setupMenu() {
    val glass = ItemStack(Material.CYAN_STAINED_GLASS_PANE)
    val gMeta = glass.itemMeta
    gMeta.displayName(Component.empty())
    glass.itemMeta = gMeta

    for (x in 1..9) {
      if (x != 3 && x != 5 && x != 7) setItem(1, x, glass)
    }

    val divider = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
    val dMeta = divider.itemMeta
    dMeta.displayName(Component.empty())
    divider.itemMeta = dMeta
    for (x in 1..9) setItem(2, x, divider)

    for (x in 1..9) setItem(6, x, glass)

    val unlock = ItemStack(Material.MAGMA_CREAM)
    val uMeta = unlock.itemMeta
    uMeta.displayName(Component.text("スキル解放", NamedTextColor.GOLD))
    unlock.itemMeta = uMeta

    addButton(6, 6, unlock) { _ -> SkillUnlockUI(player).open(player, this) }

    wireStaticButtons()
    updateUI()
  }

  private fun wireStaticButtons() {}

  private fun updateUI() {
    clearButtons()
    setupMenuFrameOnly()
    registerUnlockButton()
    registerDynamicButtons()
    registerBackButton()
  }

  private fun registerUnlockButton() {
    val unlock = ItemStack(Material.MAGMA_CREAM)
    val uMeta = unlock.itemMeta
    uMeta.displayName(Component.text("スキル解放", NamedTextColor.GOLD))
    unlock.itemMeta = uMeta

    addButton(6, 6, unlock) { event ->
      event.isCancelled = true
      SkillUnlockUI(player).open(player, this)
    }
  }

  private fun setupMenuFrameOnly() {
    updateEquippedSlots()
    updateSkillList()
  }

  private fun registerDynamicButtons() {
    val upSlot = toIndex(3, 9)
    val downSlot = toIndex(5, 9)

    addButtonAt(upSlot, inventory.getItem(upSlot) ?: ItemStack(Material.AIR)) { _ ->
      if (page > 0) {
        page--
        updateSkillList()
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        registerListButtons()
        registerPagerButtons()
      }
    }

    addButtonAt(downSlot, inventory.getItem(downSlot) ?: ItemStack(Material.AIR)) { _ ->
      val skills = playerData.unlockedSkills.mapNotNull { SkillRegistry.get(it) }
      val maxPage = (skills.size - 1) / pageSize
      if (page < maxPage) {
        page++
        updateSkillList()
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        registerListButtons()
        registerPagerButtons()
      }
    }

    registerListButtons()
  }

  private fun registerBackButton() {
    val back = ItemStack(Material.ARROW)
    val backMeta = back.itemMeta
    backMeta.displayName(Component.text("戻る", NamedTextColor.RED))
    back.itemMeta = backMeta

    addButton(6, 5, back) { event ->
      event.isCancelled = true
      val prev = getPreviousUI()
      if (prev != null) {
        prev.open(player, null)
      } else {
        player.closeInventory()
      }
    }
  }

  private fun registerPagerButtons() {
    val upSlot = toIndex(3, 9)
    val downSlot = toIndex(5, 9)

    addButtonAt(upSlot, inventory.getItem(upSlot) ?: ItemStack(Material.AIR)) { _ ->
      if (page > 0) {
        page--
        updateSkillList()
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        registerListButtons()
        registerPagerButtons()
      }
    }

    addButtonAt(downSlot, inventory.getItem(downSlot) ?: ItemStack(Material.AIR)) { _ ->
      val skills = playerData.unlockedSkills.mapNotNull { SkillRegistry.get(it) }
      val maxPage = (skills.size - 1) / pageSize
      if (page < maxPage) {
        page++
        updateSkillList()
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        registerListButtons()
        registerPagerButtons()
      }
    }
  }

  private fun registerListButtons() {
    for (row in 3..5) {
      for (col in 2..8) {
        val slot = toIndex(row, col)
        addButtonAt(slot, inventory.getItem(slot) ?: ItemStack(Material.AIR)) { _ ->
          val listRow = row - 3
          val listCol = col - 2
          val listIndex = listRow * 7 + listCol
          val realIndex = (page * pageSize) + listIndex

          val skills = playerData.unlockedSkills.mapNotNull { SkillRegistry.get(it) }
          if (realIndex in skills.indices) {
            val selectedSkill = skills[realIndex]
            if (!playerData.equippedSkills.containsValue(selectedSkill.id)) {
              equipSkill(selectedSkill)
            } else {
              val slotEntry =
                playerData.equippedSkills.entries.find {
                  it.value == selectedSkill.id
                }
              if (slotEntry != null) {
                handleUnequip(slotEntry.key)
              }
            }
            updateEquippedSlots()
            updateSkillList()
            registerListButtons()
            registerPagerButtons()
          }
        }
      }
    }
  }

  private fun updateEquippedSlots() {
    setEquippedSlot(3, SkillSlot.SHIFT_LEFT_CLICK, "シフト + 左クリック")
    setEquippedSlot(5, SkillSlot.SHIFT_RIGHT_CLICK, "シフト + 右クリック")
    setEquippedSlot(7, SkillSlot.Q, "Qキー")

    addButtonAt(2, inventory.getItem(2) ?: ItemStack(Material.AIR)) { _ ->
      handleUnequip(SkillSlot.SHIFT_LEFT_CLICK)
    }
    addButtonAt(4, inventory.getItem(4) ?: ItemStack(Material.AIR)) { _ ->
      handleUnequip(SkillSlot.SHIFT_RIGHT_CLICK)
    }
    addButtonAt(6, inventory.getItem(6) ?: ItemStack(Material.AIR)) { _ ->
      handleUnequip(SkillSlot.Q)
    }
  }

  private fun setEquippedSlot(col: Int, type: SkillSlot, label: String) {
    val row = 1

    val skillId = playerData.equippedSkills[type]
    val item: ItemStack

    if (skillId != null) {
      val skill = SkillRegistry.get(skillId)
      if (skill != null) {
        item = skill.icon.clone()
        val meta = item.itemMeta
        meta.displayName(Component.text(skill.name, NamedTextColor.GREEN))
        val lore =
          listOf(
            Component.text(label, NamedTextColor.GOLD),
            Component.text("クリックで解除", NamedTextColor.GRAY),
          )
        meta.lore(lore)
        item.itemMeta = meta
      } else {
        item = ItemStack(Material.BARRIER)
      }
    } else {
      item = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
      val meta = item.itemMeta
      meta.displayName(Component.text("空きスロット", NamedTextColor.GRAY))
      val lore =
        listOf(
          Component.text(label, NamedTextColor.GOLD),
          Component.text("下のリストから選択", NamedTextColor.GRAY),
        )
      meta.lore(lore)
      item.itemMeta = meta
    }

    setItem(row, col, item)
  }

  private fun updateSkillList() {
    val skills = playerData.unlockedSkills.mapNotNull { SkillRegistry.get(it) }
    val maxPage = (skills.size - 1) / pageSize

    for (y in 3..5) {
      for (x in 2..8) {
        setItem(y, x, ItemStack(Material.AIR))
      }
    }
    for (y in 3..5) {
      setItem(y, 9, ItemStack(Material.CYAN_STAINED_GLASS_PANE))
      setItem(y, 1, ItemStack(Material.CYAN_STAINED_GLASS_PANE))
    }

    if (skills.isNotEmpty()) {
      if (page > 0) {
        val up = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val um = up.itemMeta
        um.displayName(Component.text("前のページ"))
        up.itemMeta = um
        setItem(3, 9, up)
      }

      val ind = ItemStack(Material.PAPER)
      val im = ind.itemMeta
      im.displayName(Component.text("${page + 1} / ${maxPage + 1}"))
      ind.itemMeta = im
      setItem(4, 9, ind)

      if (page < maxPage) {
        val down = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val dm = down.itemMeta
        dm.displayName(Component.text("次のページ"))
        down.itemMeta = dm
        setItem(5, 9, down)
      }
    }

    val startIndex = page * pageSize
    var currentIndex = 0
    var currentRow = 3
    var currentCol = 2

    for (i in startIndex until skills.size) {
      if (currentIndex >= pageSize) break
      val skill = skills[i]

      val isEquipped = playerData.equippedSkills.containsValue(skill.id)

      val item = skill.icon.clone()
      val meta = item.itemMeta

      if (isEquipped) {
        meta.displayName(Component.text(skill.name + " (装備中)", NamedTextColor.GREEN))
        val lore = listOf(Component.text("装備済み", NamedTextColor.GRAY))
        meta.lore(lore)
      } else {
        meta.displayName(Component.text(skill.name, NamedTextColor.WHITE))
        val lore = listOf(Component.text("クリックして装備", NamedTextColor.YELLOW))
        meta.lore(lore)
      }
      item.itemMeta = meta
      setItem(currentRow, currentCol, item)

      currentCol++
      if (currentCol > 8) {
        currentCol = 2
        currentRow++
      }
      currentIndex++
    }
  }

  override fun onClick(event: InventoryClickEvent) {
    super.onClick(event)
  }

  private fun handleUnequip(slot: SkillSlot) {
    if (playerData.equippedSkills[slot] != null) {
      playerData.equippedSkills[slot] = null
      updateEquippedSlots()
      updateSkillList()
      player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
    }
  }

  private fun equipSkill(skill: Skill) {
    val emptySlot = playerData.equippedSkills.entries
      .firstOrNull { it.value == null }
      ?.key

    if (emptySlot != null) {
      playerData.equippedSkills[emptySlot] = skill.id
      updateEquippedSlots()
      updateSkillList()
      player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
    } else {
      player.sendMessage(Component.text("スロットがいっぱいです。解除してください。", NamedTextColor.RED))
      player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
    }
  }
}
