package net.chikina.minecraft.dungeon.combat.skill.impl.starfall

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.effect.DungeonEffect
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.funnel.Funnel
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class CometAssistEffect(val level: Int) : DungeonEffect(60.0) {

    override fun onAttack(target: CombatEntity, damage: Double, type: DamageType) {
        val loc = owner.location.clone().add(0.0, 2.5, 0.0)
        val funnel = Funnel(owner, loc, 60L)
        funnel.flySpeed = 1.2 + (level * 0.1)

        funnel.damage = owner.stats.attack.baseAttack * 0.8
        funnel.knockbackForce = 0.2

        funnel.launch(target)
    }
}

class CometAssistSkill : Skill() {
    override val id: String = "starfall_comet_assist"
    override val icon: ItemStack = ItemStack(Material.NETHER_STAR)
    override val name: String = "Comet Assist"
    override val cooldown: Long = 200

    override fun perform(attacker: CombatEntity, target: CombatEntity?) {
        val effect = CometAssistEffect(level)
        attacker.addEffect(effect)

        val loc = attacker.location.clone().add(0.0, 2.5, 0.0)
        Funnel(attacker, loc, 60L * 20)
    }

    override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
        return emptyList()
    }
}
