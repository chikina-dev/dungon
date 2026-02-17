package net.chikina.minecraft.dungeon.util

object Setting {
  val dbUrl: String by lazy { getString("DB_URL", "jdbc:postgresql://localhost:5432/dungeon") }

  val dbUser: String by lazy { getString("DB_USER", "dungeon") }

  val dbPassword: String by lazy { getString("DB_PASSWORD", "dungeon") }

  val initialOps: List<String> by lazy { getList("INITIAL_OPS") }

  val gameRules: Map<String, String> by lazy { getMap("GAME_RULES") }

  private fun getString(key: String, default: String): String = System.getenv(key) ?: default

  private fun getList(key: String): List<String> {
    val env = System.getenv(key)
    if (env.isNullOrBlank()) {
      return emptyList()
    }
    return env.split(",").map { it.trim() }.filter { it.isNotEmpty() }
  }

  private fun getMap(key: String): Map<String, String> {
    val env = System.getenv(key)
    if (env.isNullOrBlank()) {
      return emptyMap()
    }
    return env
      .split(",")
      .mapNotNull {
        val parts = it.split(":")
        if (parts.size == 2) {
          parts[0].trim() to parts[1].trim()
        } else {
          null
        }
      }.toMap()
  }
}
