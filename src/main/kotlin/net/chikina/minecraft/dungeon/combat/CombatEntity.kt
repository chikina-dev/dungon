package net.chikina.minecraft.dungeon.combat

import net.chikina.minecraft.dungeon.Dungeon
import net.chikina.minecraft.dungeon.combat.effect.DungeonEffect
import net.chikina.minecraft.dungeon.combat.effect.VanillaEffect
import net.chikina.minecraft.dungeon.combat.effect.attribute.AttributeEffect
import net.chikina.minecraft.dungeon.combat.effect.attribute.AttributeSynergyManager
import net.chikina.minecraft.dungeon.stats.CombatStats
import net.chikina.minecraft.dungeon.util.DamageIndicator
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

abstract class CombatEntity {
    abstract val name: String
    abstract val stats: CombatStats
    abstract val location: Location

    var currentHp: Double = 0.0

    open val isDead: Boolean
        get() = currentHp <= 0

    open val level: Int
        get() = 1

    private val effects = mutableListOf<DungeonEffect>()

    fun getEffectsCopy(): List<DungeonEffect> {
        return ArrayList(effects)
    }

    private val effectsToRemove = mutableListOf<DungeonEffect>()

    open fun tick() {
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            effect.onTick()
            if (effect.isExpired) {
                iterator.remove()
            }
        }

        if (effectsToRemove.isNotEmpty()) {
            effects.removeAll(effectsToRemove)
            effectsToRemove.clear()
        }
    }

    fun addEffect(effect: DungeonEffect) {
        if (effect is AttributeEffect) {
            if (AttributeSynergyManager.applySynergy(this, effect)) {
                return
            }
        }

        if (effect is VanillaEffect) {
            val iterator = effects.iterator()
            while (iterator.hasNext()) {
                val existing = iterator.next()
                if (existing is VanillaEffect && existing.type == effect.type) {
                    existing.isExpired = true
                    existing.onRemove()
                    iterator.remove()
                }
            }
        } else {
            val iterator = effects.iterator()
            while (iterator.hasNext()) {
                val existing = iterator.next()
                if (existing::class == effect::class) {
                    if (existing.merge(effect)) {
                        return
                    }
                    existing.isExpired = true
                    existing.onRemove()
                    iterator.remove()
                }
            }
        }

        effect.owner = this
        effects.add(effect)
        effect.onApply()
    }

    fun removeEffect(effectClass: Class<out DungeonEffect>) {
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            if (effectClass.isInstance(effect)) {
                effect.isExpired = true
                effect.onRemove()
                iterator.remove()
            }
        }
    }

    fun takeDamage(context: DamageContext) {
        var multiplier = 1.0
        for (effect in effects) {
            multiplier *= effect.onDamageTaken(context)
        }

        val actualDamage = (context.amount * multiplier).coerceAtLeast(0.0)
        context.amount = actualDamage

        currentHp -= actualDamage
        if (currentHp < 0) currentHp = 0.0

        onDamageTaken(context)

        DamageIndicator.spawn(location, context)

        if (isDead) {
            onDeath(context.attacker)
        }
    }

    open fun heal(amount: Double) {
        val actualHeal = amount.coerceAtLeast(0.0)
        currentHp += actualHeal
        if (currentHp > stats.hp) currentHp = stats.hp

        onHealed(actualHeal)
    }

    open fun onAttack(target: CombatEntity, damage: Double, type: DamageType) {
        for (effect in effects) {
            effect.onAttack(target, damage, type)
        }
    }

    abstract fun getLivingEntity(): LivingEntity?
    abstract fun sendMessage(message: String)
    abstract fun sendMessage(message: Component)

    protected open fun onDamageTaken(context: DamageContext) {}
    protected open fun onHealed(amount: Double) {}
    open fun onDeath(killer: CombatEntity?) {
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            effect.onDeath()

            if (!effect.isPersistent) {
                iterator.remove()
                effect.onRemove()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CombatEntity) return false

        val thisLe = this.getLivingEntity()
        val otherLe = other.getLivingEntity()

        if (thisLe == null || otherLe == null) return false

        return thisLe.uniqueId == otherLe.uniqueId
    }

    override fun hashCode(): Int {
        return getLivingEntity()?.uniqueId?.hashCode() ?: 0
    }

    open fun onRespawn() {
        for (effect in ArrayList(effects)) {
            if (effect.isPersistent) {
                effect.onRespawn()
            }
        }
    }

    open fun onKill(victim: CombatEntity) {}

    open fun isAlly(other: CombatEntity): Boolean {
        return false
    }

    open fun consumeMana(amount: Double): Boolean {
        return true
    }

    open fun notifyRareDrop(
            dropName: Component,
            originalChance: Double,
            finalChance: Double,
            itemStack: org.bukkit.inventory.ItemStack
    ) {}

    fun notifyDamageReceived(
            amount: Double,
            type: DamageType,
            source: CombatEntity?,
            isCrit: Boolean
    ) {
        val sourceName = source?.name ?: "Unknown"
        val color =
                if (isCrit) net.kyori.adventure.text.format.NamedTextColor.GOLD
                else net.kyori.adventure.text.format.NamedTextColor.RED
        val typeText =
                when (type) {
                    DamageType.PHYSICAL -> ""
                    DamageType.MAGIC -> " (魔法)"
                    DamageType.TRUE -> " (固定)"
                }

        sendMessage(
                net.kyori.adventure.text.Component.text()
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        sourceName,
                                        net.kyori.adventure.text.format.NamedTextColor.YELLOW
                                )
                        )
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        " から ",
                                        net.kyori.adventure.text.format.NamedTextColor.GRAY
                                )
                        )
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        amount.toInt().toString(),
                                        color
                                )
                        )
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        "$typeText のダメージを受けました",
                                        net.kyori.adventure.text.format.NamedTextColor.YELLOW
                                )
                        )
                        .build()
        )
    }

    fun notifyDamageDealt(amount: Double, type: DamageType, target: CombatEntity, isCrit: Boolean) {
        val targetName = target.name
        val color =
                if (isCrit) net.kyori.adventure.text.format.NamedTextColor.GOLD
                else net.kyori.adventure.text.format.NamedTextColor.RED
        val typeText =
                when (type) {
                    DamageType.PHYSICAL -> ""
                    DamageType.MAGIC -> " (魔法)"
                    DamageType.TRUE -> " (固定)"
                }

        sendMessage(
                net.kyori.adventure.text.Component.text()
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        targetName,
                                        net.kyori.adventure.text.format.NamedTextColor.YELLOW
                                )
                        )
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        " に ",
                                        net.kyori.adventure.text.format.NamedTextColor.GRAY
                                )
                        )
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        amount.toInt().toString(),
                                        color
                                )
                        )
                        .append(
                                net.kyori.adventure.text.Component.text(
                                        "$typeText のダメージを与えました",
                                        net.kyori.adventure.text.format.NamedTextColor.YELLOW
                                )
                        )
                        .build()
        )
    }

    fun getNearbyEntities(range: Double): List<CombatEntity> {
        return getNearbyEntities(location, range, range, range)
    }

    fun getNearbyEntities(x: Double, y: Double, z: Double): List<CombatEntity> {
        return getNearbyEntities(location, x, y, z)
    }

    open fun getTarget(): CombatEntity? {
        return null
    }

    companion object {
        fun from(entity: Entity): CombatEntity? {
            if (entity is Player) {
                return Dungeon.instance.playerManager.getPlayer(entity)
            } else if (entity is LivingEntity) {
                return Dungeon.instance.enemySpawner.getEnemy(entity.uniqueId)
            } else if (entity is Projectile) {
                val shooter = entity.shooter
                if (shooter is Entity) return from(shooter)
            }
            return null
        }

        fun getNearbyEntities(location: Location, range: Double): List<CombatEntity> {
            return getNearbyEntities(location, range, range, range)
        }

        fun getNearbyEntities(
                location: Location,
                x: Double,
                y: Double,
                z: Double
        ): List<CombatEntity> {
            return location.getNearbyEntities(x, y, z).mapNotNull { from(it) }
        }
    }
}

enum class DamageType {
    PHYSICAL,
    MAGIC,
    TRUE
}
