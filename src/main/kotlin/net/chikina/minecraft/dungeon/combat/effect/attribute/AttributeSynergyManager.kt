package net.chikina.minecraft.dungeon.combat.effect.attribute

import kotlin.math.min
import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.MagicElement
import net.chikina.minecraft.dungeon.combat.effect.debuff.ElectroChargedEffect
import net.chikina.minecraft.dungeon.combat.effect.debuff.ElementalResistanceDownEffect
import org.bukkit.Particle

object AttributeSynergyManager {

    fun applySynergy(target: CombatEntity, incoming: AttributeEffect): Boolean {
        val existingEffects = target.getEffectsCopy()
        var incomingAmount = incoming.amount
        var interacted = false

        for (existing in existingEffects) {
            if (existing !is AttributeEffect) continue
            if (existing.amount <= 0) continue
            if (incomingAmount <= 0) break

            if (existing.element == incoming.element) continue

            if (canInteract(existing.element, incoming.element)) {
                val consumed = min(existing.amount, incomingAmount)

                triggerSynergy(target, existing, incoming, consumed)

                existing.amount -= consumed
                incomingAmount -= consumed

                if (existing.amount <= 0) {
                    existing.isExpired = true
                    existing.onRemove()
                }
                interacted = true
            }
        }

        incoming.amount = incomingAmount
        return incoming.amount <= 0
    }

    private fun canInteract(e1: MagicElement, e2: MagicElement): Boolean {
        return (e1 == MagicElement.FIRE && e2 == MagicElement.WATER) ||
                (e1 == MagicElement.WATER && e2 == MagicElement.FIRE) ||
                (e1 == MagicElement.WATER && e2 == MagicElement.THUNDER) ||
                (e1 == MagicElement.THUNDER && e2 == MagicElement.WATER) ||
                (e2 == MagicElement.WIND && e1 != MagicElement.WIND)
    }

    private fun triggerSynergy(
            target: CombatEntity,
            existing: AttributeEffect,
            incoming: AttributeEffect,
            amount: Double
    ) {
        val source = incoming.source
        val faith = source?.stats?.faith ?: 0.0

        val level = source?.level ?: 1

        val e1 = existing.element
        val e2 = incoming.element

        if ((e1 == MagicElement.FIRE && e2 == MagicElement.WATER) ||
                        (e1 == MagicElement.WATER && e2 == MagicElement.FIRE)
        ) {
            val baseDmg = 100.0 + (level * 10.0)
            val faithBonus = faith * 2.0
            val damage = baseDmg + faithBonus

            target.takeDamage(DamageContext(damage, DamageType.MAGIC, source))
            target.sendMessage("§b§l蒸発! §7(§f${damage.toInt()}§7)")
            target.location.world.spawnParticle(
                    Particle.CLOUD,
                    target.location.add(0.0, 1.0, 0.0),
                    20,
                    0.5,
                    0.5,
                    0.5,
                    0.1
            )
        } else if ((e1 == MagicElement.WATER && e2 == MagicElement.THUNDER) ||
                        (e1 == MagicElement.THUNDER && e2 == MagicElement.WATER)
        ) {
            val initBase = 20.0 + (level * 2.0)
            val initDmg = initBase + (faith * 0.5)

            val dotBase = 10.0 + (level * 1.0)
            val dotDmg = dotBase + (faith * 0.2)
            val duration = 5.0

            target.takeDamage(DamageContext(initDmg, DamageType.MAGIC, source))
            target.sendMessage("§e§l感電! §7(§f${initDmg.toInt()}§7)")

            target.addEffect(ElectroChargedEffect(duration, dotDmg, source))
        } else if (e2 == MagicElement.WIND && e1 != MagicElement.WIND) {
            val baseDmg = 50.0 + (level * 5.0)
            val damage = baseDmg + faith

            val otherElement = e1

            target.takeDamage(DamageContext(damage, DamageType.MAGIC, source))

            val reduction = 30.0
            target.addEffect(ElementalResistanceDownEffect(10.0, otherElement, reduction))

            target.sendMessage(
                    "§a§l拡散 (${otherElement})! §7(§f${damage.toInt()}§7) §c[耐性 -${reduction.toInt()}%]"
            )
            target.location.world.spawnParticle(
                    Particle.SWEEP_ATTACK,
                    target.location.add(0.0, 1.0, 0.0),
                    5,
                    0.5,
                    0.5,
                    0.5,
                    0.1
            )
        }
    }
}
