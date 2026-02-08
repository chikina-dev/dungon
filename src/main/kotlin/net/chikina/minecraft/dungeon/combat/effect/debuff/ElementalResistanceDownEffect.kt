package net.chikina.minecraft.dungeon.combat.effect.debuff

import net.chikina.minecraft.dungeon.combat.MagicElement
import net.chikina.minecraft.dungeon.combat.effect.DungeonEffect
import kotlin.math.max

class ElementalResistanceDownEffect(
  duration: Double,
  val element: MagicElement,
  val reductionAmount: Double,
) : DungeonEffect(duration, isPersistent = false) {
  override fun merge(newEffect: DungeonEffect): Boolean {
    if (newEffect is ElementalResistanceDownEffect && newEffect.element == this.element) {
      this.durationSeconds = max(this.durationSeconds, newEffect.durationSeconds)
      this.remainingTicks = (this.durationSeconds * 20).toLong()
      return true
    }
    return false
  }

  override fun onApply() {
    applyReduction()
  }

  override fun onRemove() {
    removeReduction()
  }

  private fun applyReduction() {
    val stats = owner.stats.defense
    when (element) {
      MagicElement.FIRE -> stats.fireDefense -= reductionAmount
      MagicElement.WATER -> stats.waterDefense -= reductionAmount
      MagicElement.THUNDER -> stats.thunderDefense -= reductionAmount
      MagicElement.WIND -> stats.windDefense -= reductionAmount
      MagicElement.EARTH -> stats.earthDefense -= reductionAmount
    }
  }

  private fun removeReduction() {
    val stats = owner.stats.defense
    when (element) {
      MagicElement.FIRE -> stats.fireDefense += reductionAmount
      MagicElement.WATER -> stats.waterDefense += reductionAmount
      MagicElement.THUNDER -> stats.thunderDefense += reductionAmount
      MagicElement.WIND -> stats.windDefense += reductionAmount
      MagicElement.EARTH -> stats.earthDefense += reductionAmount
    }
  }
}
