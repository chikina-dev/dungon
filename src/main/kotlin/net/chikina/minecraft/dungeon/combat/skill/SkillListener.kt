package net.chikina.minecraft.dungeon.combat.skill

import net.chikina.minecraft.dungeon.event.DungeonProjectileHitEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SkillListener : Listener {

    @EventHandler
    fun onDungeonProjectileHit(event: DungeonProjectileHitEvent) {
        val skill = event.skill ?: return
        skill.onProjectileHit(event.originalEvent, event.shooter)
    }
}
