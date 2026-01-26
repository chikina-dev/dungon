package net.chikina.minecraft.dungeon.util

import net.chikina.minecraft.dungeon.Dungeon
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object DungeonTask {
  fun runLater(delay: Long, task: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskLater(Dungeon.instance, task, delay)
  }

  fun runTimer(delay: Long, period: Long, task: (BukkitRunnable) -> Unit): BukkitTask {
    val runnable =
            object : BukkitRunnable() {
              override fun run() {
                task(this)
              }
            }
    return runnable.runTaskTimer(Dungeon.instance, delay, period)
  }

  fun runAsync(task: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskAsynchronously(Dungeon.instance, task)
  }

  fun runSync(task: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTask(Dungeon.instance, task)
  }

  fun run(task: () -> Unit): BukkitTask {
    return runSync(task)
  }
}
