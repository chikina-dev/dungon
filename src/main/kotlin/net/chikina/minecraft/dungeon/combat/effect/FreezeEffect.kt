package net.chikina.minecraft.dungeon.combat.effect

import org.bukkit.Particle
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class FreezeEffect(duration: Double) : DungeonEffect(duration, false) {

    override fun onApply() {
        val le = owner.getLivingEntity() ?: return
        le.addPotionEffect(
                PotionEffect(
                        PotionEffectType.SLOWNESS,
                        (durationSeconds * 20).toInt(),
                        10,
                        false,
                        false,
                        false
                )
        )
        le.addPotionEffect(
                PotionEffect(
                        PotionEffectType.JUMP_BOOST,
                        (durationSeconds * 20).toInt(),
                        128,
                        false,
                        false,
                        false
                )
        )

        owner.sendMessage("§b凍結しました!")
    }

    override fun onTick() {
        super.onTick()
        val loc = owner.location.add(0.0, 1.0, 0.0)
        loc.world?.spawnParticle(Particle.SNOWFLAKE, loc, 5, 0.5, 0.5, 0.5, 0.0)
    }

    override fun onRemove() {
        val le = owner.getLivingEntity() ?: return
        le.removePotionEffect(PotionEffectType.SLOWNESS)
        le.removePotionEffect(PotionEffectType.JUMP_BOOST)
        owner.sendMessage("§b凍結が解除されました。")
    }
}
