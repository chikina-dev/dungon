package net.chikina.minecraft.dungeon.gathering

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.floor

object LootCalculator {
  fun calculateDropMultiplier(fortune: Int): Int {
    val baseMult = floor(fortune / 100.0).toInt()
    val chance = fortune % 100
    val roll = ThreadLocalRandom.current().nextInt(100)
    val bonus = if (roll < chance) 1 else 0
    return 1 + baseMult + bonus
  }
}
