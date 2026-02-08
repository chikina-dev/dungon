package net.chikina.minecraft.dungeon.combat.skill

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

object SkillRegistry {
  private val skills = mutableMapOf<String, Skill>()
  private var plugin: Plugin? = null

  fun init(plugin: Plugin) {
    this.plugin = plugin
  }

  fun register(skill: Skill) {
    skills[skill.id] = skill

    // Auto-register skills that implement Listener
    if (skill is Listener && plugin != null) {
      Bukkit.getPluginManager().registerEvents(skill, plugin!!)
    }
  }

  fun get(id: String): Skill? = skills[id]

  fun getAll(): Collection<Skill> = skills.values
}
