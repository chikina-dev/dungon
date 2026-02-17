package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.SkillRequirement
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.ItemSkillHelper
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GrapplingHookSkill :
  Skill(),
  Listener {
  companion object {
    private val anchors = ConcurrentHashMap<UUID, Location>()
    private val flyingHooks = ConcurrentHashMap<UUID, BukkitTask>()

    fun hasAnchor(playerId: UUID): Boolean = anchors.containsKey(playerId)

    fun isFlying(playerId: UUID): Boolean = flyingHooks.containsKey(playerId)
  }

  override val id: String = "grappling_hook"
  override val name: String = "Grappling Hook"
  override val cooldown: Long = 0
  override val icon: ItemStack = ItemStack(Material.FISHING_ROD)
  override val manaCost: Double = 0.0
  override val dependency: SkillRequirement? = null

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val player = attacker.getLivingEntity() as? Player ?: return
    activate(player)
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> = emptyList()

  private fun activate(player: Player) {
    val playerId = player.uniqueId

    when {
      anchors.containsKey(playerId) -> {
        performSwing(player, anchors[playerId]!!)
        removeAnchor(player)
      }

      flyingHooks.containsKey(playerId) -> {
        flyingHooks[playerId]?.cancel()
        flyingHooks.remove(playerId)
        player.sendMessage(Component.text("Hook cancelled.", NamedTextColor.YELLOW))
      }

      else -> {
        launchCustomProjectile(player)
      }
    }
  }

  private fun launchCustomProjectile(player: Player) {
    var location = player.eyeLocation
    val velocity = player.location.direction.multiply(2.2)
    val range = 40.0
    var distanceTraveled = 0.0

    fun removeFlight() {
      flyingHooks.remove(player.uniqueId)
    }

    val task =
      DungeonTask.runTimer(0L, 1L) { task ->
        if (!flyingHooks.containsKey(player.uniqueId)) {
          task.cancel()
          return@runTimer
        }

        if (ItemSkillHelper.getWeaponSkill(player.inventory.itemInMainHand) !=
          "grappling_hook"
        ) {
          removeFlight()
          task.cancel()
          return@runTimer
        }

        val speed = velocity.length()
        val result =
          location.world?.rayTrace(
            location,
            velocity.clone().normalize(),
            speed,
            FluidCollisionMode.NEVER,
            true,
            0.5,
          ) { it != player }

        if (result != null) {
          val hitLoc = result.hitPosition.toLocation(location.world!!)
          createAnchor(player, hitLoc)
          removeFlight()
          task.cancel()
          return@runTimer
        }

        location.add(velocity)
        distanceTraveled += speed
        location.world?.spawnParticle(Particle.CRIT, location, 1, 0.0, 0.0, 0.0, 0.0)

        if (distanceTraveled >= range) {
          removeFlight()
          task.cancel()
        }
      }

    flyingHooks[player.uniqueId] = task
    player.playSound(player.location, Sound.ENTITY_FISHING_BOBBER_THROW, 1f, 1.2f)
  }

  private fun createAnchor(player: Player, location: Location) {
    anchors[player.uniqueId] = location
    player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 0.5f, 2f)
    player.sendMessage(Component.text("Hooked!", NamedTextColor.GREEN))
    spawnParticleLine(player.eyeLocation, location)
  }

  private fun performSwing(player: Player, anchor: Location) {
    val lookDir = player.location.direction
    val toAnchor = anchor.toVector().subtract(player.location.toVector())

    val pullVector = toAnchor.clone().normalize().multiply(1.5)
    val steerVector = lookDir.clone().multiply(0.5)
    val velocity = pullVector.add(steerVector).add(Vector(0.0, 0.1, 0.0))

    player.velocity = velocity
    player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f)
    spawnParticleLine(player.eyeLocation, anchor)
  }

  private fun spawnParticleLine(start: Location, end: Location) {
    val distance = start.distance(end)
    if (distance > 50) return

    val gap = 0.3
    val vector = end
      .toVector()
      .subtract(start.toVector())
      .normalize()
      .multiply(gap)
    val points = (distance / gap).toInt()

    var current = start.clone()
    for (i in 0 until points) {
      current.add(vector)
      current.world?.spawnParticle(Particle.CRIT, current, 1, 0.0, 0.0, 0.0, 0.0)
    }
  }

  private fun removeAnchor(player: Player) {
    anchors.remove(player.uniqueId)
  }

  private fun cleanup(player: Player) {
    removeAnchor(player)
    flyingHooks.remove(player.uniqueId)?.cancel()
  }

  // ===================== Event Handlers =====================

  @EventHandler
  fun onInteract(event: PlayerInteractEvent) {
    val item = event.item ?: return
    if (ItemSkillHelper.getWeaponSkill(item) == "grappling_hook") {
      if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
        event.isCancelled = true
      }
    }
  }

  @EventHandler
  fun onFish(event: PlayerFishEvent) {
    if (ItemSkillHelper.getWeaponSkill(event.player.inventory.itemInMainHand) ==
      "grappling_hook" ||
      ItemSkillHelper.getWeaponSkill(event.player.inventory.itemInOffHand) ==
      "grappling_hook"
    ) {
      event.isCancelled = true
    }
  }

  @EventHandler
  fun onSneak(event: PlayerToggleSneakEvent) {
    if (event.isSneaking && anchors.containsKey(event.player.uniqueId)) {
      removeAnchor(event.player)
    }
  }

  @EventHandler
  fun onQuit(event: PlayerQuitEvent) {
    cleanup(event.player)
  }

  @EventHandler
  fun onTeleport(event: PlayerTeleportEvent) {
    cleanup(event.player)
  }

  @EventHandler
  fun onItemHeld(event: PlayerItemHeldEvent) {
    val player = event.player
    if (anchors.containsKey(player.uniqueId) || flyingHooks.containsKey(player.uniqueId)) {
      cleanup(player)
    }
  }

  @EventHandler
  fun onDrop(event: PlayerDropItemEvent) {
    val player = event.player
    if (ItemSkillHelper.getWeaponSkill(event.itemDrop.itemStack) == "grappling_hook") {
      cleanup(player)
    }
  }

  @EventHandler
  fun onSwapHand(event: PlayerSwapHandItemsEvent) {
    val player = event.player
    if (anchors.containsKey(player.uniqueId) || flyingHooks.containsKey(player.uniqueId)) {
      cleanup(player)
    }
  }

  @EventHandler
  fun onInventoryClick(event: InventoryClickEvent) {
    val player = event.whoClicked as? Player ?: return
    if (event.slot == player.inventory.heldItemSlot) {
      if (anchors.containsKey(player.uniqueId) || flyingHooks.containsKey(player.uniqueId)) {
        cleanup(player)
      }
    }
  }

  @EventHandler
  fun onDeath(event: PlayerDeathEvent) {
    cleanup(event.entity)
  }
}
