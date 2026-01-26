package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageCalculator
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.skill.Skill
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class BasicAttackSkill : Skill() {
    override val id: String = "basic_attack"
    override val icon: ItemStack = ItemStack(Material.IRON_SWORD)
    override val name: String = "Basic Attack"
    override val cooldown: Long = 0

    override fun perform(attacker: CombatEntity, target: CombatEntity?) {
        val targets = if (target != null) listOf(target) else getTargets(attacker)

        if (targets.isEmpty()) return

        for (t in targets) {
            val damageResult = DamageCalculator.calculateDamage(attacker, t)

            applyDamage(
                    t,
                    DamageContext(
                            damageResult.totalDamage,
                            DamageType.PHYSICAL,
                            attacker,
                            damageResult.isCrit
                    )
            )
        }
    }

    override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
        val target = attacker.getTarget() ?: return emptyList()
        return listOf(target)
    }
}
