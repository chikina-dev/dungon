package net.chikina.minecraft.dungeon.enemy.impl

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.combat.skill.impl.starfall.*
import net.chikina.minecraft.dungeon.enemy.DungeonBoss
import net.chikina.minecraft.dungeon.enemy.EnemyDrop
import net.chikina.minecraft.dungeon.enemy.EnemyEquipment
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.GameMaterial
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.util.DungeonTask
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

class StarfallCleric : DungeonBoss("Starfall Cleric", EntityType.SKELETON, 1200L) {
    override val baseRunes: Long = 5000

    private val skillStarBit = StarBitSkill()
    private val skillVeil = AstralVeilSkill()
    private val skillAssist = CometAssistSkill()
    private val skillJudgement = JudgementStarfallSkill()
    private val skillRush = MeteorRushSkill()

    private var aiTask: BukkitTask? = null

    init {
        skillStarBit.level = 0
        skillVeil.level = 1
        skillAssist.level = 2
        skillJudgement.level = 4
        skillRush.level = 3
    }

    override fun initializeStats() {
        stats.hp = 50000.0
        stats.attack.baseAttack = 100.0
        stats.defense.baseDefense = 20.0
        stats.speed = 0.25
    }

    override fun getEquipment(): EnemyEquipment {
        return EnemyEquipment(
                helmet = GameItem(ItemStack(Material.GOLDEN_HELMET)),
                chestplate = GameItem(ItemStack(Material.IRON_CHESTPLATE)),
                mainHand = GameItem(ItemStack(Material.END_ROD))
        )
    }

    override fun getDrops(): List<EnemyDrop> {
        return listOf(
                EnemyDrop(GameItem.from(GameMaterial.STAR_FRAGMENT, 1), 1.0, 1, 3),
                EnemyDrop(GameItem.from(GameMaterial.STAR_DUST, 1), 1.0, 2, 5),
                EnemyDrop(GameItem.from(GameMaterial.COSMIC_SHARD, 1), 0.5, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.STELLAR_CORE, 1), 0.1, 1, 1),
                EnemyDrop(GameItem.from(GameMaterial.GALACTIC_ESSENCE, 1), 0.01, 1, 1)
        )
    }

    override fun spawn(location: Location) {
        super.spawn(location)
        startAI()
        castSkill(skillAssist, null)
    }

    override fun despawn() {
        aiTask?.cancel()
        aiTask = null
        super.despawn()
    }

    private enum class Phase {
        ONE,
        TWO,
        THREE
    }

    private var currentPhase = Phase.ONE
    private val lastSkillTime = mutableMapOf<Skill, Long>()

    private var lastHeartbeatTime = 0L
    private val HEARTBEAT_INTERVAL_MS = 1000L

    private fun startAI() {
        aiTask =
                DungeonTask.runTimer(20L, 1L) { task ->
                    if (isDead || entity == null || !entity!!.isValid) {
                        task.cancel()
                        return@runTimer
                    }
                    this@StarfallCleric.tick()

                    val hpRatio = currentHp / stats.hp
                    if (currentPhase == Phase.ONE && hpRatio <= 0.6) {
                        currentPhase = Phase.TWO
                        sendMessage("§c§l[Starfall Cleric]§r §eThe stars... they are aligning!")
                        castSkill(skillVeil, null)
                        Dungeon.instance.server.broadcast(
                                Component.text("§eStarfall Cleric has entered Phase 2!")
                        )
                    } else if (currentPhase == Phase.TWO && hpRatio <= 0.3) {
                        currentPhase = Phase.THREE
                        sendMessage("§c§l[Starfall Cleric]§r §4§lBEHOLD THE COSMOS!")
                        castSkill(skillVeil, null)
                        castSkill(skillAssist, null)
                        Dungeon.instance.server.broadcast(
                                Component.text("§c§lStarfall Cleric has entered FINAL Phase!")
                        )
                    }

                    val now = System.currentTimeMillis()
                    if (now - lastHeartbeatTime < HEARTBEAT_INTERVAL_MS) return@runTimer
                    lastHeartbeatTime = now

                    val target = getTargetPlayer() ?: return@runTimer
                    val dist = location.distance(target.location)

                    when (currentPhase) {
                        Phase.ONE -> runPhaseOne(target, dist)
                        Phase.TWO -> runPhaseTwo(target, dist)
                        Phase.THREE -> runPhaseThree(target, dist)
                    }
                }
    }

    private fun runPhaseOne(target: DungeonPlayer, dist: Double) {
        if (dist < 5.0) {
            tryCast(skillRush, null, 1.0)
        }

        if (dist > 15.0) {
            tryCast(skillStarBit, target, 0.8)
        } else {
            tryCast(skillStarBit, target, 0.6)
        }
    }

    private fun runPhaseTwo(target: DungeonPlayer, dist: Double) {
        if (tryCast(skillJudgement, target, 1.0)) {
            DungeonTask.runLater(20L) { castSkill(skillStarBit, target) }
        }

        if (dist < 8.0) {
            tryCast(skillRush, null, 0.5)
        }

        tryCast(skillStarBit, target, 0.5)

        if (currentHp < stats.hp * 0.5) {
            tryCast(skillVeil, null, 0.2)
        }
    }

    private fun runPhaseThree(target: DungeonPlayer, dist: Double) {
        tryCast(skillStarBit, target, 0.9)
        tryCast(skillJudgement, target, 1.0)
        tryCast(skillRush, null, 0.9)
        tryCast(skillVeil, null, 0.5)
    }

    private fun tryCast(skill: Skill, target: CombatEntity?, chance: Double): Boolean {
        if (Math.random() > chance) return false
        val now = System.currentTimeMillis()
        val last = lastSkillTime.getOrDefault(skill, 0L)
        val cdMs = skill.cooldown * 50

        if (now - last >= cdMs) {
            castSkill(skill, target)
            lastSkillTime[skill] = now
            return true
        }
        return false
    }

    private fun getTargetPlayer(): DungeonPlayer? {
        val nearby = location.getNearbyEntities(20.0, 20.0, 20.0)
        return nearby.firstNotNullOfOrNull {
            if (it is Player) Dungeon.instance.playerManager.getPlayer(it) else null
        }
    }
}
