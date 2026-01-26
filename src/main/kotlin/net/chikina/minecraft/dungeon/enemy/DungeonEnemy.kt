package net.chikina.minecraft.dungeon.enemy

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.stats.CombatStats
import net.chikina.minecraft.dungeon.util.Log
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute as BukkitAttribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType

abstract class DungeonEnemy(
        override val name: String,
        val entityType: EntityType,
        val respawnTime: Long
) : CombatEntity() {
    override val stats: CombatStats = CombatStats()

    var entity: LivingEntity? = null
    var spawnLocation: Location? = null

    abstract val baseRunes: Long
    abstract fun getEquipment(): EnemyEquipment

    @Suppress("DEPRECATION")
    open fun spawn(location: Location) {
        spawnLocation = location
        val world = location.world ?: return
        val spawned = world.spawnEntity(location, entityType) as? LivingEntity ?: return
        entity = spawned

        setupPersistentData(spawned)
        initializeStats()
        currentHp = stats.hp
        configureVisuals(spawned)
        applyEquipment(spawned)
    }

    private fun setupPersistentData(entity: LivingEntity) {
        val pdc = entity.persistentDataContainer
        pdc.set(
                NamespacedKey(Dungeon.instance, "dungeon_enemy"),
                PersistentDataType.BYTE,
                1.toByte()
        )
        pdc.set(
                NamespacedKey(Dungeon.instance, "dungeon_enemy_type"),
                PersistentDataType.STRING,
                this::class.simpleName ?: "Unknown"
        )
    }

    private fun configureVisuals(entity: LivingEntity) {
        entity.customName(Component.text(name))
        entity.isCustomNameVisible = true

        val maxHealthCap = 1024.0
        val visualMaxHealth = stats.hp.coerceAtMost(maxHealthCap)

        runCatching {
            entity.getAttribute(BukkitAttribute.valueOf("GENERIC_MAX_HEALTH"))?.baseValue =
                    visualMaxHealth
        }
        entity.health = visualMaxHealth
    }

    private fun applyEquipment(entity: LivingEntity) {
        val equipment = getEquipment()
        entity.equipment?.let { equip ->
            equipment.helmet?.let { equip.helmet = it.itemStack }
            equipment.chestplate?.let { equip.chestplate = it.itemStack }
            equipment.leggings?.let { equip.leggings = it.itemStack }
            equipment.boots?.let { equip.boots = it.itemStack }
            equipment.mainHand?.let { equip.setItemInMainHand(it.itemStack) }
            equipment.offHand?.let { equip.setItemInOffHand(it.itemStack) }
        }
    }

    open fun despawn() {
        entity?.remove()
        entity = null
    }

    override val location: Location
        get() =
                entity?.location
                        ?: spawnLocation
                                ?: throw IllegalStateException("敵がスポーンしておらず、スポーン地点も設定されていません")

    override fun onDamageTaken(context: DamageContext) {
        updateVisuals()
    }

    override fun onHealed(amount: Double) {
        updateVisuals()
    }

    override fun isAlly(other: CombatEntity): Boolean {
        return other is DungeonEnemy
    }

    override fun onDeath(killer: CombatEntity?) {
        runCatching {
            val dropLocation = this.location
            killer?.onKill(this)
            processItemDrops(killer, dropLocation)
        }
                .onFailure { e -> Log.warn("Error processing death for $name: ${e.message}") }
        despawn()
    }

    private fun processItemDrops(killer: CombatEntity?, dropLocation: Location) {
        val killerDropRate = killer?.stats?.itemDropRate ?: 0.0
        val chanceMultiplier = 1.0 + (killerDropRate / 100.0)

        for (drop in getDrops()) {
            val finalChance = drop.chance * chanceMultiplier

            if (Math.random() <= finalChance) {
                val amount =
                        if (drop.minAmount == drop.maxAmount) {
                            drop.minAmount
                        } else {
                            (drop.minAmount..drop.maxAmount).random()
                        }

                if (amount > 0) {
                    val itemStack = drop.item.itemStack.clone()
                    itemStack.amount = amount
                    dropLocation.world?.dropItemNaturally(dropLocation, itemStack)

                    if (drop.chance <= RARE_DROP_THRESHOLD && killer != null) {
                        killer.notifyRareDrop(
                                itemStack.itemMeta.displayName()
                                        ?: Component.text(itemStack.type.name),
                                drop.chance,
                                finalChance,
                                itemStack
                        )
                    }
                }
            }
        }
    }

    abstract fun getDrops(): List<EnemyDrop>

    open fun updateVisuals() {
        entity?.customName(Component.text("$name [HP: ${currentHp.toInt()}/${stats.hp.toInt()}]"))
    }

    abstract fun initializeStats()

    override fun getLivingEntity(): LivingEntity? {
        return entity
    }

    override fun getTarget(): CombatEntity? {
        val target = (entity as? org.bukkit.entity.Mob)?.target ?: return null
        return CombatEntity.from(target)
    }

    override fun sendMessage(message: String) {}

    override fun sendMessage(message: Component) {}

    companion object {
        private const val RARE_DROP_THRESHOLD = 0.05
    }
}
