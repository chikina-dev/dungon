package net.chikina.minecraft.dungeon.util

import net.chikina.minecraft.dungeon.Dungeon
import java.util.logging.Level

object Log {
  fun info(message: String) {
    Dungeon.instance.logger.info(message)
  }

  fun warn(message: String) {
    Dungeon.instance.logger.warning(message)
  }

  fun error(message: String) {
    Dungeon.instance.logger.severe(message)
  }

  fun error(message: String, e: Throwable) {
    Dungeon.instance.logger.log(Level.SEVERE, message, e)
  }
}
