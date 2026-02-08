package net.chikina.minecraft.dungeon.portal

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.ui.Sidebar
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class PortalListener(
  private val portalConfig: PortalConfig,
) : Listener {
  private val cooldowns = mutableMapOf<UUID, Long>()
  private val cooldownMs = 3000L

  @EventHandler
  fun onPortal(event: PlayerPortalEvent) {
    if (event.cause != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return

    val player = event.player
    val fromLoc = event.from

    val now = System.currentTimeMillis()
    val lastUse = cooldowns[player.uniqueId] ?: 0L
    if (now - lastUse < cooldownMs) {
      event.isCancelled = true
      Messenger.error(player, "クールダウン中です。少々お待ちください。")
      return
    }

    var finalDest: Location? = null
    var requiredLevel = 0

    val portalByEntrance = portalConfig.getPortalByEntry(fromLoc)
    if (portalByEntrance != null) {
      requiredLevel = portalByEntrance.requiredLevel
      val source = portalByEntrance.entrance
      val baseDest = portalByEntrance.exit.clone()

      baseDest.yaw = source.yaw
      baseDest.pitch = source.pitch
      finalDest = baseDest.add(baseDest.direction.multiply(1.0))
    } else {
      val portalByExit = portalConfig.getPortalByExit(fromLoc)
      if (portalByExit != null) {
        requiredLevel = portalByExit.requiredLevel
        val source = portalByExit.exit
        val baseDest = portalByExit.entrance.clone()

        baseDest.yaw = source.yaw
        baseDest.pitch = source.pitch
        finalDest = baseDest.add(baseDest.direction.multiply(1.0))
      }
    }

    if (finalDest != null) {
      if (player.level < requiredLevel) {
        Messenger.error(player, "このポータルを使用するにはレベル $requiredLevel が必要です。")
        event.isCancelled = true
        return
      }

      event.isCancelled = true

      val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
      val bonus = dungeonPlayer.extractRunes(1.2)
      if (bonus > 0) {
        Messenger.send(
          player,
          Component.text(
            "ポータル脱出成功！ ルーンを $bonus (120%) 獲得しました！",
            NamedTextColor.LIGHT_PURPLE,
          ),
        )
        Sidebar.update(player)
      }

      player.teleport(finalDest)
      cooldowns[player.uniqueId] = now
    }
  }

  @EventHandler
  fun onEntityPortal(event: EntityPortalEvent) {
    val entity = event.entity
    val pdc = entity.persistentDataContainer
    val key = NamespacedKey(Dungeon.instance, "dungeon_enemy")

    if (pdc.has(key, PersistentDataType.BYTE)) {
      event.isCancelled = true
    }
  }
}
