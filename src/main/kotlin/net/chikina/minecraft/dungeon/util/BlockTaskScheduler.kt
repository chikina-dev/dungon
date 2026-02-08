package net.chikina.minecraft.dungeon.util

import org.bukkit.scheduler.BukkitTask

/** Helper to process a list of items over time (e.g. restoring blocks). */
object BlockTaskScheduler {
  /**
   * Runs a task that processes [items] in chunks of [perTick] every [period] ticks.
   * @param items List of items to process
   * @param perTick Number of items to process per tick
   * @param period Ticks between executions (default 1)
   * @param initialDelay Ticks before first execution (default 0)
   * @param action Action to perform on each item
   * @return The active BukkitTask
   */
  fun <T> run(
    items: List<T>,
    perTick: Int = 20,
    period: Long = 1L,
    initialDelay: Long = 0L,
    onComplete: (() -> Unit)? = null,
    action: (T) -> Unit,
  ): BukkitTask {
    // Copy list to avoid concurrent modification if source changes
    val queue = ArrayList(items)
    var index = 0

    return DungeonTask.runTimer(initialDelay, period) { task ->
      if (index >= queue.size) {
        task.cancel()
        onComplete?.invoke()
        return@runTimer
      }

      repeat(perTick) {
        if (index < queue.size) {
          action(queue[index])
          index++
        }
      }
    }
  }
}
