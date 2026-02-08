package net.chikina.minecraft.dungeon.util

import org.bukkit.configuration.file.FileConfiguration

object DevToggles {
  private const val KEY_UNLOCK_TEST_SKILLS = "dev.unlockTestSkillsOnFirstJoin"

  fun unlockTestSkillsOnFirstJoin(config: FileConfiguration): Boolean = config.getBoolean(KEY_UNLOCK_TEST_SKILLS, false)
}
