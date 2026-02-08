package net.chikina.minecraft.dungeon.util

import net.chikina.minecraft.dungeon.combat.DamageContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import java.text.DecimalFormat

object DamageIndicator {
  private val format = DecimalFormat("#,###")

  fun spawn(location: Location, context: DamageContext) {
    val world = location.world ?: return

    val offsetLoc =
      location
        .clone()
        .add(
          (Math.random() - 0.5) * 2.0,
          (Math.random() * 0.5) + 1.0,
          (Math.random() - 0.5) * 2.0,
        )

    val display = world.spawnEntity(offsetLoc, EntityType.TEXT_DISPLAY) as TextDisplay

    val dmgText = format.format(context.amount.toInt())
    val textComponent =
      if (context.isCrit) {
        Component.text("✧$dmgText✧", NamedTextColor.YELLOW)
      } else {
        Component.text(dmgText, NamedTextColor.GRAY)
      }

    display.text(textComponent)
    display.billboard = Display.Billboard.CENTER
    display.isShadowed = true
    display.backgroundColor = Color.fromARGB(0, 0, 0, 0)
    DungeonTask.runLater(20L) { display.remove() }
  }
}
