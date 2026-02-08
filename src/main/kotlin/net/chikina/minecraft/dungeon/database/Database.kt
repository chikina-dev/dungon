package net.chikina.minecraft.dungeon.database

import java.sql.Connection

interface Database {
  fun connect()

  fun disconnect()

  fun getConnection(): Connection
}
