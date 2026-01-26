package net.chikina.minecraft.dungeon.event

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.SkillRegistry
import net.chikina.minecraft.dungeon.input.PlayerAction
import net.chikina.minecraft.dungeon.ui.DungeonUI
import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.persistence.PersistentDataType

class EventNormalizer : Listener {

  companion object {
    // Track recent actions to prevent duplicate firing
    private val recentActions = ConcurrentHashMap<UUID, Long>()
    private const val DUPLICATE_THRESHOLD_MS = 50L

    private fun shouldProcess(playerId: UUID): Boolean {
      val now = System.currentTimeMillis()
      val last = recentActions[playerId] ?: 0L
      if (now - last < DUPLICATE_THRESHOLD_MS) {
        return false
      }
      recentActions[playerId] = now
      return true
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onInteract(event: PlayerInteractEvent) {
    val player = event.player

    val isLeftClick = event.action.name.contains("LEFT")
    val isRightClick = event.action.name.contains("RIGHT")
    if (!isLeftClick && !isRightClick) return

    // Deduplication check
    if (!shouldProcess(player.uniqueId)) return

    val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
    val isSneaking = player.isSneaking

    val playerAction =
            when {
              isSneaking && isLeftClick -> PlayerAction.SHIFT_LEFT_CLICK
              isSneaking && isRightClick -> PlayerAction.SHIFT_RIGHT_CLICK
              isLeftClick -> PlayerAction.LEFT_CLICK
              else -> PlayerAction.RIGHT_CLICK
            }

    val actionEvent = PlayerActionEvent(dungeonPlayer, playerAction, null, event)
    Bukkit.getPluginManager().callEvent(actionEvent)

    if (actionEvent.isCancelled) {
      event.isCancelled = true
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onDrop(event: PlayerDropItemEvent) {
    val player = event.player
    val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)

    if (event.itemDrop.itemStack.type == Material.NETHER_STAR) {
      event.isCancelled = true
      return
    }

    // Mark this tick as having a Q action (prevents left click from also firing)
    recentActions[player.uniqueId] = System.currentTimeMillis()

    val actionEvent = PlayerActionEvent(dungeonPlayer, PlayerAction.Q, null, event)
    Bukkit.getPluginManager().callEvent(actionEvent)

    if (actionEvent.isCancelled) {
      event.isCancelled = true
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onSwap(event: PlayerSwapHandItemsEvent) {
    if (event.offHandItem.type == Material.NETHER_STAR ||
                    event.mainHandItem.type == Material.NETHER_STAR
    ) {
      event.isCancelled = true
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onEntityDamage(event: EntityDamageByEntityEvent) {
    val attacker = CombatEntity.from(event.damager)
    val victim = CombatEntity.from(event.entity)

    if (attacker != null && victim != null) {
      val dungeonEvent = DungeonCombatEvent(attacker, victim, event)
      Bukkit.getPluginManager().callEvent(dungeonEvent)
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onInventoryClick(event: InventoryClickEvent) {
    val holder = event.view.topInventory.holder
    if (holder is DungeonUI && event.whoClicked is Player) {
      val dungeonEvent =
              DungeonUIClickEvent(holder, event.whoClicked as Player, event.rawSlot, event)
      Bukkit.getPluginManager().callEvent(dungeonEvent)
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onInventoryClose(event: InventoryCloseEvent) {
    val holder = event.inventory.holder
    if (holder is DungeonUI && event.player is Player) {
      val dungeonEvent = DungeonUICloseEvent(holder, event.player as Player, event)
      Bukkit.getPluginManager().callEvent(dungeonEvent)
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onSwing(event: PlayerAnimationEvent) {
    if (event.animationType == PlayerAnimationType.ARM_SWING) {
      val player = event.player
      val targetBlock = player.getTargetBlockExact(5)
      if (targetBlock != null) {
        val dungeonEvent = DungeonMiningSwingEvent(player, targetBlock, event)
        Bukkit.getPluginManager().callEvent(dungeonEvent)
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onBreak(event: BlockBreakEvent) {
    val dungeonEvent = DungeonBlockBreakEvent(event.player, event.block, event)
    Bukkit.getPluginManager().callEvent(dungeonEvent)
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onProjectileHit(event: ProjectileHitEvent) {
    val projectile = event.entity
    val container = projectile.persistentDataContainer

    if (container.has(PluginKeys.SKILL_ID, PersistentDataType.STRING)) {
      val skillId = container.get(PluginKeys.SKILL_ID, PersistentDataType.STRING)
      val skill = SkillRegistry.get(skillId!!) ?: return

      val shooter = projectile.shooter
      if (shooter is Entity) {
        val combatAttacker = CombatEntity.from(shooter)
        if (combatAttacker != null) {
          val dungeonEvent = DungeonProjectileHitEvent(projectile, combatAttacker, skill, event)
          Bukkit.getPluginManager().callEvent(dungeonEvent)
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  fun onMove(event: PlayerMoveEvent) {
    val from = event.from
    val to = event.to ?: return

    if (from.x == to.x && from.z == to.z) return

    val player = event.player
    val dungeonPlayer = Dungeon.instance.playerManager.getPlayer(player)
    val dx = to.x - from.x
    val dz = to.z - from.z

    if (dx == 0.0 && dz == 0.0) return

    val yaw = Math.toRadians(player.location.yaw.toDouble())
    val sinYaw = Math.sin(yaw)
    val cosYaw = Math.cos(yaw)

    val forward = -(dx * sinYaw + dz * cosYaw)
    val strafe = dx * cosYaw - dz * sinYaw
    val actions = mutableListOf<PlayerAction>()

    if (Math.abs(forward) > 0.01) {
      if (forward > 0) {
        actions.add(PlayerAction.MOVE_FORWARD)
      } else {
        actions.add(PlayerAction.MOVE_BACKWARD)
      }
    }

    if (Math.abs(strafe) > 0.01) {
      if (strafe > 0) {
        actions.add(PlayerAction.MOVE_LEFT)
      } else {
        actions.add(PlayerAction.MOVE_RIGHT)
      }
    }

    for (action in actions) {
      val actionEvent = PlayerActionEvent(dungeonPlayer, action, null, event)
      Bukkit.getPluginManager().callEvent(actionEvent)
    }
  }
}
