package net.chikina.minecraft.dungeon.input

import net.chikina.minecraft.dungeon.combat.skill.SkillSlot
import net.chikina.minecraft.dungeon.event.PlayerActionEvent
import net.chikina.minecraft.dungeon.ui.MenuUI
import net.chikina.minecraft.dungeon.util.ItemSkillHelper
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerInputListener : Listener {
  @EventHandler
  fun onAction(event: PlayerActionEvent) {
    val player = event.player
    val action = event.action

    when (action) {
      PlayerAction.SHIFT_LEFT_CLICK -> {
        val skillId = player.playerData.equippedSkills[SkillSlot.SHIFT_LEFT_CLICK]
        if (!skillId.isNullOrEmpty()) {
          player.castSkill(skillId)
          event.isCancelled = true
        }
      }

      PlayerAction.SHIFT_RIGHT_CLICK -> {
        val skillId = player.playerData.equippedSkills[SkillSlot.SHIFT_RIGHT_CLICK]
        if (!skillId.isNullOrEmpty()) {
          player.castSkill(skillId)
          event.isCancelled = true
        }
      }

      PlayerAction.Q -> {
        val skillId = player.playerData.equippedSkills[SkillSlot.Q]
        if (!skillId.isNullOrEmpty()) {
          val dropEvent = event.originalEvent as? PlayerDropItemEvent
          val item = dropEvent?.itemDrop?.itemStack
          player.castSkill(skillId, item)
          event.isCancelled = true
        }
      }

      PlayerAction.RIGHT_CLICK -> {
        val item = player.player.inventory.itemInMainHand

        // Menu (Nether Star)
        if (item.type == Material.NETHER_STAR) {
          player.player.openInventory(MenuUI(player.player).inventory)
          event.isCancelled = true
          return
        }

        // Weapon Skill (generic)
        val weaponSkillId = ItemSkillHelper.getWeaponSkill(item)
        if (weaponSkillId != null) {
          player.castSkill(weaponSkillId)
          event.isCancelled = true
        }
      }

      PlayerAction.LEFT_CLICK -> {}

      PlayerAction.MOVE_FORWARD,
      PlayerAction.MOVE_BACKWARD,
      PlayerAction.MOVE_LEFT,
      PlayerAction.MOVE_RIGHT,
      -> {}
    }
  }
}
