package net.chikina.minecraft.dungeon.ui

import io.papermc.paper.scoreboard.numbers.NumberFormat
import net.chikina.minecraft.dungeon.Dungeon
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

object Sidebar {

    fun update(player: Player) {
        val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
        val data = dungeonPlayer.playerData

        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val objective =
                scoreboard.registerNewObjective(
                        "sidebar",
                        Criteria.DUMMY,
                        Component.text("Dungeon", NamedTextColor.GOLD)
                                .decorate(TextDecoration.UNDERLINED)
                )
        objective.displaySlot = DisplaySlot.SIDEBAR

        val lines =
                listOf(
                        Component.empty(),
                        Component.text("Level: ", NamedTextColor.GRAY)
                                .append(Component.text(data.level, NamedTextColor.GREEN)),
                        Component.text("Runes: ", NamedTextColor.GRAY)
                                .append(Component.text(data.runes, NamedTextColor.GOLD))
                                .append(
                                        if (data.accumulatedRunes > 0)
                                                Component.text(
                                                        " (+${data.accumulatedRunes})",
                                                        NamedTextColor.AQUA
                                                )
                                        else Component.empty()
                                ),
                        Component.empty(),
                        Component.text("Stat Points: ", NamedTextColor.GRAY)
                                .append(Component.text(data.statPoints, NamedTextColor.AQUA)),
                        Component.empty(),
                        Component.text("----------------", NamedTextColor.DARK_GRAY)
                )
        var scoreVal = 0

        for (line in lines) {
            val entryKey = "§${Integer.toHexString(scoreVal)}"

            val score = objective.getScore(entryKey)
            score.score = scoreVal

            score.customName(line)
            score.numberFormat(NumberFormat.blank())

            scoreVal--
        }

        player.scoreboard = scoreboard
    }
}
