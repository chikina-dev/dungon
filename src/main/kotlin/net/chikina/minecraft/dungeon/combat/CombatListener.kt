package net.chikina.minecraft.dungeon.combat

import net.chikina.minecraft.dungeon.combat.skill.SkillSlot
import net.chikina.minecraft.dungeon.combat.skill.impl.BasicAttackSkill
import net.chikina.minecraft.dungeon.event.DungeonCombatEvent
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.player.PlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CombatListener(
  private val playerManager: PlayerManager,
) : Listener {
  private val basicAttack = BasicAttackSkill()

  @EventHandler
  fun onDungeonCombat(event: DungeonCombatEvent) {
    event.isCancelled = true

    val combatDamager = event.attacker
    val combatTarget = event.victim
    val originalEvent = event.originalEvent
    val damagerEntity = originalEvent.damager

    val customDamage =
      if (damagerEntity.hasMetadata("custom_damage")) {
        damagerEntity.getMetadata("custom_damage").firstOrNull()?.asDouble()
      } else {
        null
      }

    val battle = CombatBattle(combatDamager, combatTarget)

    if (customDamage != null) {
      val context = DamageContext(customDamage, DamageType.MAGIC, combatDamager, false)
      combatTarget.takeDamage(context)
    } else {
      if (combatDamager is DungeonPlayer) {
        val player = combatDamager.player
        if (player.isSneaking) {
          val shiftSkill =
            combatDamager.playerData.equippedSkills[SkillSlot.SHIFT_LEFT_CLICK]
          if (!shiftSkill.isNullOrEmpty()) {
            return
          }
        }
      }
      battle.performAction(basicAttack)
    }
  }
}
