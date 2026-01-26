package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack

class TestHealSkill : Skill() {
    override val id: String = "test_heal"
    override val name: String = "Healing Light"
    override val cooldown: Long = 5000
    override val icon: ItemStack = ItemStack(Material.GOLDEN_APPLE)

    private val healAmount: Double = 5.0

    override fun perform(attacker: CombatEntity, target: CombatEntity?) {
        attacker.heal(healAmount)
        attacker.getLivingEntity()
                ?.world
                ?.spawnParticle(
                        Particle.HEART,
                        attacker.location.add(0.0, 2.0, 0.0),
                        5,
                        0.5,
                        0.5,
                        0.5
                )
        attacker.sendMessage(
                Component.text("You healed yourself for $healAmount HP!", NamedTextColor.GREEN)
        )
    }

    override fun getTargets(attacker: CombatEntity): List<CombatEntity> {
        return emptyList()
    }
}
