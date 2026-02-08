package net.chikina.minecraft.dungeon.combat.skill.impl.starfall

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.effect.AstralVeilEffect
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class AstralVeilSkill : Skill() {
  override val id: String = "starfall_astral_veil"
  override val icon: ItemStack = ItemStack(Material.NETHER_STAR)
  override val name: String = "Astral Veil"
  override val cooldown: Long = 600

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val effect = AstralVeilEffect(level)
    attacker.addEffect(effect)

    attacker.sendMessage(Component.text("Astral Veil activated!", NamedTextColor.AQUA))
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> = emptyList()
}
