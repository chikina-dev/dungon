package net.chikina.minecraft.dungeon.game

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.combat.skill.impl.MagicBasicAttackSkill
import net.chikina.minecraft.dungeon.item.WandItem
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class WandListener : Listener {
  private val magicAttack = MagicBasicAttackSkill()

  @EventHandler
  fun onInteract(event: PlayerInteractEvent) {
    if (event.hand != EquipmentSlot.HAND) return

    val item = event.item ?: return
    if (item.type == Material.AIR) return

    val wand = WandItem(item)
    if (wand.element == null) return

    if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
      val player = event.player
      if (player.isSneaking) return

      val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)

      if (!dungeonPlayer.checkAndConsumeMana(magicAttack.manaCost)) {
        return
      }

      magicAttack.perform(dungeonPlayer, null)

      if (event.action == Action.LEFT_CLICK_BLOCK) {
        event.isCancelled = true
      }
    }
  }
}
