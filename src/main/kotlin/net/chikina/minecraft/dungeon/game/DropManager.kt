package net.chikina.minecraft.dungeon.game

import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.UUID

class DropManager : Listener {
  fun dropPrivateItem(location: Location, itemStack: ItemStack, owner: Player) {
    // dropItem drops deeper into the block/ground than dropItemNaturally, but allows consumer
    // modifications before spawn
    location.world.dropItem(location, itemStack) { item ->
      // Prevent scattering
      item.velocity = Vector(0, 0, 0)

      // Visuals
      item.isGlowing = true
      // Note: glowing color is usually determined by team color.
      // If we want white, we might need to rely on the client's default or manage teams.
      // Default glowing is white/team color.

      // Ownership
      val container = item.persistentDataContainer
      container.set(PluginKeys.itemOwnerKey, PersistentDataType.STRING, owner.uniqueId.toString())

      // Metadata for quick access/other plugins if needed? Not necessary if using PDC.
      // Attributes: Invulnerability?
      item.isInvulnerable = true
      item.setCanMobPickup(false)
    }
  }

  @EventHandler
  fun onPickup(event: EntityPickupItemEvent) {
    val entity = event.entity
    if (entity !is Player) return

    val item = event.item
    val container = item.persistentDataContainer

    if (!container.has(PluginKeys.itemOwnerKey, PersistentDataType.STRING)) {
      return
    }

    val ownerUUIDString =
      container.get(PluginKeys.itemOwnerKey, PersistentDataType.STRING) ?: return
    val ownerUUID =
      try {
        UUID.fromString(ownerUUIDString)
      } catch (e: IllegalArgumentException) {
        return
      }

    if (entity.uniqueId != ownerUUID) {
      event.isCancelled = true
      // Optional: Send message or action bar "It's not yours!"
    }
  }
}
