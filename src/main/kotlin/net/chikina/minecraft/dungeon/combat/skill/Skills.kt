package net.chikina.minecraft.dungeon.combat.skill

import net.chikina.minecraft.dungeon.combat.skill.impl.*

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
