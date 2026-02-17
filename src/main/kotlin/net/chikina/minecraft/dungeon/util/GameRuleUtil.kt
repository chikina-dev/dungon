package net.chikina.minecraft.dungeon.util

import org.bukkit.GameRule
import org.bukkit.World
import java.lang.reflect.Modifier

object GameRuleUtil {
  private fun getGameRule(name: String): GameRule<*>? {
    var rule = GameRule.getByName(name)
    if (rule != null) return rule

    val allRules = GameRule.values()

    rule = allRules.find {
      val normalizedRuleName = it.name.substringAfter(":").replace("_", "")
      val normalizedInput = name.replace("_", "")
      normalizedRuleName.equals(normalizedInput, ignoreCase = true)
    }
    if (rule != null) return rule

    val constantName = name.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").uppercase()
    try {
      val field = GameRule::class.java.getField(constantName)
      if (Modifier.isStatic(field.modifiers)) {
        @Suppress("UNCHECKED_CAST")
        return field.get(null) as? GameRule<*>
      }
    } catch (e: NoSuchFieldException) {
      // Ignore
    }

    return null
  }

  fun applyGameRules(world: World, rules: Map<String, String>) {
    rules.forEach { (inputName, inputValue) ->
      val rule = getGameRule(inputName)
      if (rule != null) {
        applyRule(world, rule, inputValue)
      } else {
        Log.warn("GameRule '$inputName' not found")
      }
    }
  }

  private fun <T> applyRule(world: World, rule: GameRule<T>, value: String) {
    try {
      if (rule.type == Boolean::class.javaObjectType) {
        @Suppress("UNCHECKED_CAST")
        val boolRule = rule as GameRule<Boolean>
        if (world.setGameRule(boolRule, value.toBoolean())) {
          Log.info("Set GameRule ${rule.name} to $value")
        } else {
          Log.warn("Failed to set Boolean GameRule ${rule.name}")
        }
      } else if (rule.type == Int::class.javaObjectType) {
        @Suppress("UNCHECKED_CAST")
        val intRule = rule as GameRule<Int>
        val intValue = value.toIntOrNull()
        if (intValue != null && world.setGameRule(intRule, intValue)) {
          Log.info("Set GameRule ${rule.name} to $value")
        } else {
          Log.warn("Failed to set Int GameRule ${rule.name} with value $value")
        }
      } else {
        Log.warn("Unsupported GameRule type for ${rule.name}: ${rule.type.simpleName}")
      }
    } catch (e: Exception) {
      Log.error("Error setting GameRule ${rule.name}", e)
    }
  }
}
