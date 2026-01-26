package net.chikina.minecraft.dungeon.combat.effect.attribute

import kotlin.math.max
import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.MagicElement
import net.chikina.minecraft.dungeon.combat.effect.DungeonEffect

abstract class AttributeEffect(
        duration: Double,
        val element: MagicElement,
        var amount: Double,
        val source: CombatEntity?
) : DungeonEffect(duration, isPersistent = false) {

    override fun merge(newEffect: DungeonEffect): Boolean {
        if (newEffect is AttributeEffect && newEffect.element == this.element) {
            this.amount += newEffect.amount
            this.durationSeconds = max(this.durationSeconds, newEffect.durationSeconds)
            this.remainingTicks = (this.durationSeconds * 20).toLong()
            return true
        }
        return false
    }

    override fun onTick() {
        super.onTick()
        playEffect()
    }

    abstract fun playEffect()
}
