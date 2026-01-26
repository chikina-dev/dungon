package net.chikina.minecraft.dungeon.player

import java.util.*
import net.chikina.minecraft.dungeon.combat.skill.SkillSlot
import net.chikina.minecraft.dungeon.stats.StatType

data class PlayerData(
        val uuid: UUID,
        var runes: Long = 0,
        var accumulatedRunes: Long = 0,
        var level: Int = 1,
        var statPoints: Int = 0,
        var allocations: MutableMap<StatType, Int> = mutableMapOf(),
        var equippedSkills: MutableMap<SkillSlot, String?> =
                mutableMapOf(
                        SkillSlot.SHIFT_LEFT_CLICK to null,
                        SkillSlot.SHIFT_RIGHT_CLICK to null,
                        SkillSlot.Q to null
                ),
        var unlockedSkills: MutableSet<String> = mutableSetOf()
) {
    fun serializeAllocations(): String {
        return allocations.entries.joinToString(",") { "${it.key.name}:${it.value}" }
    }

    fun serializeEquippedSkills(): String {
        return equippedSkills.entries.filter { it.value != null }.joinToString(",") {
            "${it.key}:${it.value}"
        }
    }

    fun serializeUnlockedSkills(): String {
        return unlockedSkills.joinToString(",")
    }

    companion object {
        fun deserializeAllocations(data: String): MutableMap<StatType, Int> {
            if (data.isBlank()) return mutableMapOf()
            return data.split(",")
                    .map { it.split(":") }
                    .filter { it.size == 2 }
                    .associate { StatType.valueOf(it[0]) to it[1].toInt() }
                    .toMutableMap()
        }

        fun deserializeEquippedSkills(data: String): MutableMap<SkillSlot, String?> {
            val map =
                    mutableMapOf<SkillSlot, String?>(
                            SkillSlot.SHIFT_LEFT_CLICK to null,
                            SkillSlot.SHIFT_RIGHT_CLICK to null,
                            SkillSlot.Q to null
                    )
            if (data.isBlank()) return map

            data.split(",").map { it.split(":") }.filter { it.size == 2 }.forEach {
                try {
                    val slot = SkillSlot.valueOf(it[0])
                    map[slot] = it[1]
                } catch (e: IllegalArgumentException) {}
            }
            return map
        }

        fun deserializeUnlockedSkills(data: String): MutableSet<String> {
            if (data.isBlank()) return mutableSetOf()
            return data.split(",").toMutableSet()
        }
    }
}
