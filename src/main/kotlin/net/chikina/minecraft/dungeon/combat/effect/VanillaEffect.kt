package net.chikina.minecraft.dungeon.combat.effect

import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class VanillaEffect(
        val type: PotionEffectType,
        val duration: Int,
        val amplifier: Int,
        val isAmbient: Boolean = false,
        val hasParticles: Boolean = false,
        val hasIcon: Boolean = false,
        isPersistent: Boolean = false
) : DungeonEffect(duration / 20.0, isPersistent) {

    init {
        if (duration > 1000000) {
            durationSeconds = Double.MAX_VALUE
            remainingTicks = Long.MAX_VALUE
        }
    }

    override fun onApply() {
        applyPotionEffect()
    }

    override fun onTick() {
        super.onTick()
        val livingEntity = owner.getLivingEntity() ?: return

        if (!livingEntity.hasPotionEffect(type)) {
            applyPotionEffect()
        }
    }

    override fun onRemove() {
        owner.getLivingEntity()?.removePotionEffect(type)
    }

    override fun onRespawn() {
        applyPotionEffect()
    }

    private fun applyPotionEffect() {
        owner.getLivingEntity()
                ?.addPotionEffect(
                        PotionEffect(type, duration, amplifier, isAmbient, hasParticles, hasIcon)
                )
    }
}
