package net.chikina.minecraft.dungeon.enemy

import net.chikina.minecraft.dungeon.item.GameItem

data class EnemyEquipment(
  val helmet: GameItem? = null,
  val chestplate: GameItem? = null,
  val leggings: GameItem? = null,
  val boots: GameItem? = null,
  val mainHand: GameItem? = null,
  val offHand: GameItem? = null,
) {
  companion object {
    fun create(
      helmet: GameItem? = null,
      chestplate: GameItem? = null,
      leggings: GameItem? = null,
      boots: GameItem? = null,
      mainHand: GameItem? = null,
      offHand: GameItem? = null,
    ): EnemyEquipment =
      EnemyEquipment(
        helmet = helmet,
        chestplate = chestplate,
        leggings = leggings,
        boots = boots,
        mainHand = mainHand,
        offHand = offHand,
      )
  }
}
