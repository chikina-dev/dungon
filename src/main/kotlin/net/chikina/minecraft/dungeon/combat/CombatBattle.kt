package net.chikina.minecraft.dungeon.combat

import net.chikina.minecraft.dungeon.combat.skill.Skill

class CombatBattle(val attacker: CombatEntity, val victim: CombatEntity) {
    fun performAction(skill: Skill) {
        skill.perform(attacker, victim)
    }
}
