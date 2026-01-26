package net.chikina.minecraft.dungeon.enemy

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

abstract class DungeonBoss(name: String, entityType: EntityType, respawnTime: Long) :
        DungeonEnemy(name, entityType, respawnTime) {

    protected val bossBar: BossBar =
            BossBar.bossBar(
                    Component.text(name),
                    1.0f,
                    BossBar.Color.RED,
                    BossBar.Overlay.NOTCHED_10
            )

    override fun spawn(location: Location) {
        super.spawn(location)
        showBossBarToNearbyPlayers()
    }

    override fun updateVisuals() {
        super.updateVisuals()
        val progress = (currentHp / stats.hp).coerceIn(0.0, 1.0).toFloat()
        bossBar.progress(progress)
        bossBar.name(Component.text("$name [${currentHp.toInt()}/${stats.hp.toInt()}]"))
    }

    private fun showBossBarToNearbyPlayers() {
        location.world ?: return
        // bossバー出す相手を後で考える
        location.getNearbyPlayers(100.0).forEach { bossBar.addViewer(it) }
    }

    override fun tick() {
        super.tick()

        if (entity != null && isValid()) {
            location.getNearbyPlayers(50.0).forEach { player ->
                if (!bossBar.viewers().contains(player)) {
                    bossBar.addViewer(player)
                }
            }
            bossBar.viewers().toList().forEach { viewer ->
                val audience = viewer as Audience
                if (audience is Player) {
                    if (audience.world != location.world ||
                                    audience.location.distanceSquared(location) > 100 * 100
                    ) {
                        bossBar.removeViewer(audience)
                    }
                }
            }
        }
    }

    override fun despawn() {
        bossBar.viewers().forEach { bossBar.removeViewer(it as Audience) }
        super.despawn()
    }

    override fun onDeath(killer: CombatEntity?) {
        super.onDeath(killer)
        bossBar.viewers().forEach { bossBar.removeViewer(it as Audience) }
    }

    private fun isValid(): Boolean {
        return entity != null && entity!!.isValid && !isDead
    }

    fun castSkill(skill: Skill, target: CombatEntity? = null) {
        val message =
                Component.text(name, NamedTextColor.RED)
                        .append(Component.text("はスキル「", NamedTextColor.YELLOW))
                        .append(Component.text(skill.name, NamedTextColor.GOLD))
                        .append(Component.text("」を使用した！", NamedTextColor.YELLOW))

        bossBar.viewers().forEach { viewer ->
            (viewer as net.kyori.adventure.audience.Audience).sendMessage(message)
        }

        skill.perform(this, target)
    }
}
