package net.chikina.minecraft.dungeon.stats

import net.chikina.minecraft.dungeon.mining.MiningStats
import net.chikina.minecraft.dungeon.player.PlayerData

object StatCalculator {
  fun calculateCombatStats(data: PlayerData, baseStats: CombatStats) {
    val allocations = data.allocations

    val vit = allocations.getOrDefault(StatType.VITALITY, 0)
    baseStats.hp += vit * HP_PER_VIT
    baseStats.hpRegen = 1.0 + (vit * HP_REGEN_PER_VIT)

    val end = allocations.getOrDefault(StatType.ENDURANCE, 0)
    baseStats.defense.baseDefense += end * DEF_PER_END
    baseStats.maxPoise += BASE_POISE + (end * POISE_PER_END)
    baseStats.poiseRegen += POISE_REGEN_BASE

    val str = allocations.getOrDefault(StatType.STRENGTH, 0)
    baseStats.attack.baseAttack += str * ATK_PER_STR

    val int = allocations.getOrDefault(StatType.INTELLIGENCE, 0)
    baseStats.attack.magicAttack += int * MAGIC_ATK_PER_INT
    baseStats.maxMana += BASE_MANA + (int * MANA_PER_INT)

    val mag = allocations.getOrDefault(StatType.MAGIC, 0)
    baseStats.manaRegen = (baseStats.maxMana * 0.01) + (mag * MANA_REGEN_PER_MAG)

    val dex = allocations.getOrDefault(StatType.DEXTERITY, 0)
    baseStats.critDamage += dex * CRIT_DMG_PER_DEX
    baseStats.critRate += dex * CRIT_RATE_PER_DEX

    val fth = allocations.getOrDefault(StatType.FAITH, 0)
    baseStats.faith += fth.toDouble()
    val fthBonus = fth * ELEMENT_PER_FTH
    baseStats.attack.fireAttack += fthBonus
    baseStats.attack.waterAttack += fthBonus
    baseStats.attack.thunderAttack += fthBonus
    baseStats.attack.windAttack += fthBonus
    baseStats.attack.earthAttack += fthBonus

    val fate = allocations.getOrDefault(StatType.FATE, 0)
    baseStats.itemDropRate += fate * DROP_RATE_PER_FATE
    baseStats.critRate += fate * CRIT_RATE_PER_FATE
    baseStats.critDamage += fate * CRIT_DMG_PER_FATE
  }

  fun calculateMiningStats(data: PlayerData): MiningStats {
    val stats = MiningStats()
    val str = data.allocations.getOrDefault(StatType.STRENGTH, 0)
    if (str > STR_THRESHOLD_FOR_MINING) {
      stats.breakingPower += (str / MINING_POWER_DIVISOR)
    }
    return stats
  }

  private const val HP_PER_VIT = 5.0
  private const val DEF_PER_END = 1.0
  private const val ATK_PER_STR = 2.0
  private const val MAGIC_ATK_PER_INT = 1.0
  private const val BASE_MANA = 100.0
  private const val MANA_PER_INT = 10.0
  private const val CRIT_DMG_PER_DEX = 0.8
  private const val CRIT_RATE_PER_DEX = 0.05
  private const val ELEMENT_PER_FTH = 0.5
  private const val DROP_RATE_PER_FATE = 0.5
  private const val CRIT_RATE_PER_FATE = 0.2
  private const val CRIT_DMG_PER_FATE = 0.05
  private const val STR_THRESHOLD_FOR_MINING = 10
  private const val MINING_POWER_DIVISOR = 20
  private const val HP_REGEN_PER_VIT = 0.5
  private const val MANA_REGEN_PER_MAG = 2.0
  private const val BASE_POISE = 30.0
  private const val POISE_PER_END = 2.0
  private const val POISE_REGEN_BASE = 5.0
}
