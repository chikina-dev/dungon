package net.chikina.minecraft.dungeon.item

import org.bukkit.inventory.ItemStack

class MiningItem(
  override val itemStack: ItemStack,
) : GameItem {
  val miningSpeed: Int
    get() {
      var speed = 0
      val id = itemId
      if (id != null) {
        speed = PickaxeItem.getById(id)?.speed ?: AxeItem.getById(id)?.speed ?: 0
      }
      if (speed == 0) {
        val mat = itemStack.type
        speed =
          PickaxeItem.values().find { it.material == mat }?.speed
            ?: AxeItem.values().find { it.material == mat }?.speed ?: 0
      }
      return speed
    }

  val breakingPower: Int
    get() {
      var power = 0
      val id = itemId
      if (id != null) {
        power = PickaxeItem.getById(id)?.power ?: AxeItem.getById(id)?.power ?: 0
      }
      if (power == 0) {
        val mat = itemStack.type
        power =
          PickaxeItem.values().find { it.material == mat }?.power
            ?: AxeItem.values().find { it.material == mat }?.power ?: 0
      }
      return power
    }

  val miningFortune: Int
    get() {
      var fortune = 0
      val id = itemId
      if (id != null) {
        fortune = PickaxeItem.getById(id)?.fortune ?: AxeItem.getById(id)?.fortune ?: 0
      }
      if (fortune == 0) {
        val mat = itemStack.type
        fortune =
          PickaxeItem.values().find { it.material == mat }?.fortune
            ?: AxeItem.values().find { it.material == mat }?.fortune ?: 0
      }
      return fortune
    }
}
