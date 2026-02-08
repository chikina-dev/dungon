package net.chikina.minecraft.dungeon.combat.skill

import net.chikina.minecraft.dungeon.combat.skill.impl.BasicAttackSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.ChainLightningSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.GrapplingHookSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.HeavyFireballSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.IceSpearSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.MagicBasicAttackSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.MeteorSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.TestFireballSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.TestHealSkill
import net.chikina.minecraft.dungeon.combat.skill.impl.WindGustSkill

object Skills {
  fun all(): List<Skill> =
    listOf(
      BasicAttackSkill(),
      MagicBasicAttackSkill(),
      TestHealSkill(),
      TestFireballSkill(),
      GrapplingHookSkill(),
      HeavyFireballSkill(),
      WindGustSkill(),
      ChainLightningSkill(),
      MeteorSkill(),
      IceSpearSkill(),
    )
}
