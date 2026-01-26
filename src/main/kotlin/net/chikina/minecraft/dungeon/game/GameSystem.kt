package net.chikina.minecraft.dungeon.game

interface GameSystem {
  fun onEnable() {}

  fun onDisable() {}

  fun update()
}
