package net.chikina.minecraft.dungeon.foraging

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import kotlin.math.ceil

object TreeVisualizer {
  fun updateHologram(tree: TreeStructure, remainingMillis: Long) {
    if (tree.worldId == null) return
    val world = Bukkit.getWorld(tree.worldId!!) ?: return

    val loc = tree.center ?: return
    val seconds = ceil(remainingMillis / 1000.0).toInt().coerceAtLeast(0)
    val text = "再生成まであと${seconds}秒"

    if (tree.displayId == null) {
      val display = world.spawn(loc, TextDisplay::class.java)
      display.text(Component.text(text))
      display.billboard = Display.Billboard.CENTER
      display.isPersistent = false
      display.isSeeThrough = true
      display.backgroundColor = Color.fromARGB(0, 0, 0, 0)
      tree.displayId = display.uniqueId
    } else {
      val entity = Bukkit.getEntity(tree.displayId!!)
      if (entity is TextDisplay) {
        entity.text(Component.text(text))
      } else {
        tree.displayId = null
      }
    }
  }

  fun removeHologram(tree: TreeStructure) {
    if (tree.displayId != null) {
      val entity = Bukkit.getEntity(tree.displayId!!)
      entity?.remove()
      tree.displayId = null
    }
  }
}
