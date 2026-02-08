package net.chikina.minecraft.dungeon.game

import net.chikina.minecraft.dungeon.ui.ArmorShopUI
import net.chikina.minecraft.dungeon.ui.AxeShopUI
import net.chikina.minecraft.dungeon.ui.ForgeShopUI
import net.chikina.minecraft.dungeon.ui.PickaxeShopUI
import net.chikina.minecraft.dungeon.ui.WandShopUI
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class NPCListener : Listener {
  @EventHandler
  fun onVillagerInteract(event: PlayerInteractEntityEvent) {
    if (event.rightClicked.type == EntityType.VILLAGER) {
      val customName = event.rightClicked.customName() ?: return
      val plainName = PlainTextComponentSerializer.plainText().serialize(customName)

      if (plainName.contains(NPC_PICKAXE_SHOP)) {
        event.isCancelled = true
        PickaxeShopUI().open(event.player, null)
      } else if (plainName.contains(NPC_AXE_SHOP)) {
        event.isCancelled = true
        AxeShopUI().open(event.player, null)
      } else if (plainName.contains(NPC_FORGE_SHOP)) {
        event.isCancelled = true
        ForgeShopUI().open(event.player, null)
      } else if (plainName.contains(NPC_WAND_SHOP)) {
        event.isCancelled = true
        WandShopUI().open(event.player, null)
      } else if (plainName.contains(NPC_ARMOR_SHOP)) {
        event.isCancelled = true
        ArmorShopUI().open(event.player, null)
      }
    }
  }

  companion object {
    private const val NPC_PICKAXE_SHOP = "Pickaxe Shop"
    private const val NPC_AXE_SHOP = "Axe Shop"
    private const val NPC_FORGE_SHOP = "Forge Shop"
    private const val NPC_WAND_SHOP = "Wand Shop"
    private const val NPC_ARMOR_SHOP = "Armor Shop"
  }
}
