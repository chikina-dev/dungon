package net.chikina.minecraft.dungeon.combat.skill

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.GameMaterial
import net.chikina.minecraft.dungeon.item.ItemAttribute
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.Material
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class Skill {
  abstract val id: String
  abstract val name: String
  abstract val cooldown: Long
  abstract val icon: ItemStack

  open val manaCost: Double = 0.0

  open val dependency: SkillRequirement? = WeaponRequirement(WeaponType.SWORD)

  var level: Int = 0
    set(value) {
      field = value.coerceIn(0, 4)
    }

  open val unlockMaterial: GameMaterial? = null

  open val poiseDamage: Double = 10.0

  abstract fun perform(attacker: CombatEntity, target: CombatEntity? = null)

  abstract fun getTargets(attacker: CombatEntity): List<CombatEntity>

  fun applyDamage(target: CombatEntity, context: DamageContext) {
    context.poiseDamage = this.poiseDamage
    target.takeDamage(context)

    target.notifyDamageReceived(context.amount, context.type, context.attacker, context.isCrit)
    context.attacker?.notifyDamageDealt(context.amount, context.type, target, context.isCrit)
  }

  open fun onProjectileHit(event: ProjectileHitEvent, attacker: CombatEntity) {}
}

enum class SkillSlot {
  SHIFT_LEFT_CLICK,
  SHIFT_RIGHT_CLICK,
  Q,
}

interface SkillRequirement {
  fun isMet(player: DungeonPlayer, item: ItemStack? = null): Boolean

  fun getDescription(): String
}

class WeaponRequirement(
  private val type: WeaponType,
) : SkillRequirement {
  override fun isMet(player: DungeonPlayer, item: ItemStack?): Boolean {
    val checkItem = item ?: player.player.inventory.itemInMainHand
    if (checkItem.type == Material.AIR) return false
    val gameItem = GameItem(checkItem)

    return when (type) {
      WeaponType.SWORD -> {
        checkItem.type.name.contains("SWORD") &&
          gameItem.hasAttribute(ItemAttribute.WEAPON)
      }

      WeaponType.WAND -> {
        gameItem.hasAttribute(ItemAttribute.WAND)
      }

      WeaponType.AXE -> {
        checkItem.type.name.contains("AXE") &&
          gameItem.hasAttribute(ItemAttribute.WEAPON)
      }

      WeaponType.ANY -> {
        gameItem.hasAttribute(ItemAttribute.WEAPON)
      }
    }
  }

  override fun getDescription(): String = when (type) {
    WeaponType.SWORD -> "剣が必要"
    WeaponType.WAND -> "杖が必要"
    WeaponType.AXE -> "斧が必要"
    WeaponType.ANY -> "武器が必要"
  }
}

class SpecificItemRequirement(
  private val itemId: String,
) : SkillRequirement {
  override fun isMet(player: DungeonPlayer, item: ItemStack?): Boolean {
    val checkItem = item ?: player.player.inventory.itemInMainHand
    val meta = checkItem.itemMeta ?: return false
    val currentId =
      meta.persistentDataContainer.get(PluginKeys.itemId, PersistentDataType.STRING)
    return currentId == itemId
  }

  override fun getDescription(): String = "専用武器が必要"
}

class CompositeRequirement(
  private val requirements: List<SkillRequirement>,
) : SkillRequirement {
  override fun isMet(player: DungeonPlayer, item: ItemStack?): Boolean = requirements.all { it.isMet(player, item) }

  override fun getDescription(): String = requirements.joinToString(", ") { it.getDescription() }
}

enum class WeaponType {
  SWORD,
  WAND,
  AXE,
  ANY,
}
