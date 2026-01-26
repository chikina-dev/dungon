package net.chikina.minecraft.dungeon

import net.chikina.minecraft.dungeon.combat.CombatListener
import net.chikina.minecraft.dungeon.combat.skill.SkillListener
import net.chikina.minecraft.dungeon.combat.skill.SkillRegistry
import net.chikina.minecraft.dungeon.combat.skill.Skills
import net.chikina.minecraft.dungeon.command.*
import net.chikina.minecraft.dungeon.database.Database
import net.chikina.minecraft.dungeon.database.impl.PostgresDatabase
import net.chikina.minecraft.dungeon.database.repository.PlayerRepository
import net.chikina.minecraft.dungeon.database.repository.SpawnPointRepository
import net.chikina.minecraft.dungeon.database.repository.impl.SqlPlayerRepository
import net.chikina.minecraft.dungeon.database.repository.impl.SqlSpawnPointRepository
import net.chikina.minecraft.dungeon.enemy.DefaultEnemyFactory
import net.chikina.minecraft.dungeon.enemy.EnemySpawner
import net.chikina.minecraft.dungeon.event.EventNormalizer
import net.chikina.minecraft.dungeon.game.GameLoop
import net.chikina.minecraft.dungeon.game.NPCListener
import net.chikina.minecraft.dungeon.game.WandListener
import net.chikina.minecraft.dungeon.game.system.EnemySpawnSystem
import net.chikina.minecraft.dungeon.game.system.EnemyUpdateSystem
import net.chikina.minecraft.dungeon.game.system.PlayerHudSystem
import net.chikina.minecraft.dungeon.game.system.PlayerRegenSystem
import net.chikina.minecraft.dungeon.game.system.PlayerSystem
import net.chikina.minecraft.dungeon.input.PlayerInputListener
import net.chikina.minecraft.dungeon.mining.core.MiningManager
import net.chikina.minecraft.dungeon.mining.core.OreRegistry
import net.chikina.minecraft.dungeon.mining.listener.MiningListener
import net.chikina.minecraft.dungeon.player.PlayerListener
import net.chikina.minecraft.dungeon.player.PlayerManager
import net.chikina.minecraft.dungeon.portal.PortalConfig
import net.chikina.minecraft.dungeon.portal.PortalListener
import net.chikina.minecraft.dungeon.ui.UIListener
import net.chikina.minecraft.dungeon.ui.shop.ShopSellListener
import net.chikina.minecraft.dungeon.util.ConfigAccessor
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.chikina.minecraft.dungeon.util.Log
import net.chikina.minecraft.dungeon.util.Messenger
import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Dungeon : JavaPlugin() {

    companion object {
        lateinit var instance: Dungeon
    }

    lateinit var enemySpawner: EnemySpawner
    lateinit var playerManager: PlayerManager
    lateinit var database: Database
    lateinit var playerRepository: PlayerRepository
    lateinit var spawnPointRepository: SpawnPointRepository
    lateinit var miningManager: MiningManager

    var isMiningSystemEnabled: Boolean = false
        private set

    override fun onEnable() {
        instance = this
        Log.info("Dungeon plugin enabled with custom mining system.")

        initializeCore()
        if (!initializeDatabase()) {
            return
        }
        initializeManagers()
        initializeSystems()
        registerListeners()
        registerCommands()
        SkillRegistry.init(this)
        registerSkills()
    }

    private fun registerSkills() {
        for (skill in Skills.all()) {
            SkillRegistry.register(skill)
        }
    }

    private fun initializeCore() {
        PluginKeys.init(this)
        OreRegistry.initialize()
    }

    private fun initializeDatabase(): Boolean {
        val dbUrl = config.getString("database.url") ?: "jdbc:postgresql://localhost:5432/dungeon"
        val dbUser = config.getString("database.username") ?: "dungeon"
        val dbPassword = config.getString("database.password") ?: "dungeon"

        database = PostgresDatabase(dbUrl, dbUser, dbPassword)

        return try {
            database.connect()
            Log.info("Connected to database.")
            playerRepository = SqlPlayerRepository(database)
            spawnPointRepository = SqlSpawnPointRepository(database)
            true
        } catch (e: Exception) {
            Log.error("Failed to connect to database", e)
            server.pluginManager.disablePlugin(this)
            false
        }
    }

    private fun initializeManagers() {
        playerManager = PlayerManager()
    }

    private fun initializeSystems() {
        enemySpawner = EnemySpawner(this, spawnPointRepository, DefaultEnemyFactory())

        GameLoop.register(PlayerSystem(playerManager))
        GameLoop.register(EnemyUpdateSystem(enemySpawner))
        GameLoop.register(EnemySpawnSystem(enemySpawner), 20)
        GameLoop.register(PlayerHudSystem(playerManager), 2)
        GameLoop.register(PlayerRegenSystem(playerManager), 20)

        DungeonTask.runTimer(0L, 1L) { GameLoop.run() }

        if (server.pluginManager.getPlugin("ProtocolLib") != null) {
            miningManager = MiningManager(this, playerManager)
            server.pluginManager.registerEvents(MiningListener(miningManager), this)
            isMiningSystemEnabled = true
            Log.info("ProtocolLib found! Custom mining system enabled.")
        } else {
            Log.warn("ProtocolLib NOT found! Custom mining system disabled.")
        }

        val configAccessor = ConfigAccessor(this, "config.yml")
        val portalConfig = PortalConfig(configAccessor)
        server.pluginManager.registerEvents(PortalListener(portalConfig), this)
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(EventNormalizer(), this)
        server.pluginManager.registerEvents(PlayerInputListener(), this)
        server.pluginManager.registerEvents(PlayerListener(playerManager, playerRepository), this)
        server.pluginManager.registerEvents(CombatListener(playerManager), this)
        server.pluginManager.registerEvents(SkillListener(), this)
        server.pluginManager.registerEvents(UIListener(), this)
        server.pluginManager.registerEvents(ShopSellListener(), this)
        server.pluginManager.registerEvents(NPCListener(), this)
        server.pluginManager.registerEvents(WandListener(), this)
    }

    private fun registerCommands() {
        getCommand("spawnshop")?.setExecutor(ShopCommand())
        getCommand("spawnforge")?.setExecutor(ForgeCommand())
        getCommand("spawnwandshop")?.setExecutor(SpawnWandShopCommand())
        getCommand("clearspawns")?.setExecutor(ClearSpawnsCommand())
        getCommand("killenemies")?.setExecutor(KillEnemiesCommand())
        getCommand("spawndummy")?.setExecutor(SpawnDummyCommand())
        getCommand("gethook")?.setExecutor(GetHookCommand())
        getCommand("spawnboss")?.setExecutor(SpawnBossCommand())

        getCommand("spawnenemy")?.setExecutor { sender, _, _, args ->
            if (sender is Player) {
                if (args.isEmpty()) {
                    Messenger.error(sender, "Usage: /spawnenemy <Type>")
                    return@setExecutor true
                }
                enemySpawner.addSpawnPoint(sender.location.clone(), args[0])
                Messenger.success(sender, "Added spawn point for Enemy ${args[0]}")
            }
            true
        }
    }

    override fun onDisable() {
        GameLoop.stop()

        if (::enemySpawner.isInitialized) {
            enemySpawner.despawnAllEnemies()
        }

        if (::database.isInitialized) {
            database.disconnect()
        }
    }
}
