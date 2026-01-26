package net.chikina.minecraft.dungeon.combat

import kotlin.random.Random

object DamageCalculator {

        data class DamageResult(
                val totalDamage: Double,
                val isCrit: Boolean,
                val attributeDamage: Map<String, Double>
        )

        fun calculateDamage(attacker: CombatEntity, defender: CombatEntity): DamageResult {
                val rawDamage = calculateBaseDamage(attacker)
                val attributeDamages = calculateAttributeDamages(attacker, defender)
                val totalAttributeDamage = attributeDamages.values.sum()

                val isCrit = rollCritical(attacker)
                val critMultiplier =
                        if (isCrit) calculateCritMultiplier(attacker, defender) else 1.0

                val defenseVal = calculateDefense(defender)
                val finalDamage =
                        ((rawDamage * critMultiplier - defenseVal).coerceAtLeast(0.0) +
                                totalAttributeDamage)

                return DamageResult(finalDamage, isCrit, attributeDamages)
        }

        private fun calculateBaseDamage(attacker: CombatEntity): Double {
                val attStats = attacker.stats.attack
                val baseDmg = attStats.baseAttack
                val typeMult = (PERCENTAGE_BASE + attStats.physicalAttack) / PERCENTAGE_BASE
                return baseDmg * typeMult
        }

        private fun calculateAttributeDamages(
                attacker: CombatEntity,
                defender: CombatEntity
        ): Map<String, Double> {
                val attStats = attacker.stats.attack
                val defStats = defender.stats.defense

                val attributes =
                        mapOf(
                                "Fire" to Pair(attStats.fireAttack, defStats.fireDefense),
                                "Water" to Pair(attStats.waterAttack, defStats.waterDefense),
                                "Thunder" to Pair(attStats.thunderAttack, defStats.thunderDefense),
                                "Wind" to Pair(attStats.windAttack, defStats.windDefense),
                                "Earth" to Pair(attStats.earthAttack, defStats.earthDefense)
                        )

                val attributeDamages = mutableMapOf<String, Double>()

                for ((attrName, attrPair) in attributes) {
                        val (attrValue, defValue) = attrPair
                        if (attrValue <= 0) continue

                        val attrDef =
                                (defStats.baseDefense / ATTRIBUTE_DIVISOR) *
                                        ((PERCENTAGE_BASE + defValue) / PERCENTAGE_BASE)
                        val attrDmgRaw =
                                (attStats.baseAttack / ATTRIBUTE_DIVISOR) *
                                        (attrValue / PERCENTAGE_BASE)

                        val finalAttrDmg = (attrDmgRaw - attrDef).coerceAtLeast(0.0)
                        if (finalAttrDmg > 0) {
                                attributeDamages[attrName] = finalAttrDmg
                        }
                }

                return attributeDamages
        }

        private fun rollCritical(attacker: CombatEntity): Boolean =
                Random.nextDouble(PERCENTAGE_BASE) < attacker.stats.critRate

        private fun calculateCritMultiplier(
                attacker: CombatEntity,
                defender: CombatEntity
        ): Double {
                val effectiveCritDmg =
                        (attacker.stats.critDamage - defender.stats.defense.critDefense)
                                .coerceAtLeast(MIN_CRIT_DAMAGE)
                return effectiveCritDmg / PERCENTAGE_BASE
        }

        private fun calculateDefense(defender: CombatEntity): Double {
                val defStats = defender.stats.defense
                val defTypeMult = (PERCENTAGE_BASE + defStats.physicalDefense) / PERCENTAGE_BASE
                return defStats.baseDefense * defTypeMult
        }

        private const val PERCENTAGE_BASE = 100.0
        private const val ATTRIBUTE_DIVISOR = 10.0
        private const val MIN_CRIT_DAMAGE = 100.0
}
