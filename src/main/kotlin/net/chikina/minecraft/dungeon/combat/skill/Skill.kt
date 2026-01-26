package net.chikina.minecraft.dungeon.combat.skill

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.item.WandItem
import net.chikina.minecraft.dungeon.player.DungeonPlayer
import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.Material
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/** すべてのスキルの基底インターフェース。 戦闘アクションを定義します。 */
abstract class Skill {
    /** スキルの一意なID */
    abstract val id: String

    /** 表示名 */
    abstract val name: String

    /** クールダウン (ms) */
    abstract val cooldown: Long

    /** アイコン (GUI表示用) */
    abstract val icon: ItemStack

    /** マナ消費量 (デフォルト: 0.0) */
    open val manaCost: Double = 0.0

    /** 発動条件 (nullなら無条件) */
    open val dependency: SkillRequirement? = null

    var level: Int = 0
        set(value) {
            field = value.coerceIn(0, 4)
        }

    /** 解放に必要な素材 (nullの場合は解放不可/初期解放) */
    open val unlockMaterial: net.chikina.minecraft.dungeon.item.GameMaterial? = null

    /**
     * スキルを実行します。
     * @param attacker 攻撃者
     * @param target 対象 (nullの場合は getTargets で取得)
     */
    abstract fun perform(attacker: CombatEntity, target: CombatEntity? = null)

    /**
     * ターゲットを自動取得します。
     * @param attacker 攻撃者
     * @return ターゲットのリスト
     */
    abstract fun getTargets(attacker: CombatEntity): List<CombatEntity>

    /** ダメージを与え、インジケーターを表示する共通メソッド。 */
    fun applyDamage(target: CombatEntity, context: DamageContext) {
        target.takeDamage(context)

        target.notifyDamageReceived(context.amount, context.type, context.attacker, context.isCrit)
        context.attacker?.notifyDamageDealt(context.amount, context.type, target, context.isCrit)
    }

    /** プロジェクタイルが着弾した時の処理ハンドラ。 SkillListenerから呼び出されます。 */
    open fun onProjectileHit(event: ProjectileHitEvent, attacker: CombatEntity) {}
}

/** スキル発動のスロット */
enum class SkillSlot {
    SHIFT_LEFT_CLICK,
    SHIFT_RIGHT_CLICK,
    Q
}

/** スキルの依存関係（発動条件） */
interface SkillRequirement {
    /** 条件を満たしているか判定する */
    fun isMet(player: DungeonPlayer): Boolean

    /** 条件の説明（UI表示用） */
    fun getDescription(): String
}

/** 武器種別の要求 */
class WeaponRequirement(private val type: WeaponType) : SkillRequirement {
    override fun isMet(player: DungeonPlayer): Boolean {
        val item = player.player.inventory.itemInMainHand
        if (item.type == Material.AIR) return false

        return when (type) {
            WeaponType.SWORD -> item.type.name.contains("SWORD")
            WeaponType.WAND -> WandItem(item).element != null
            WeaponType.AXE -> item.type.name.contains("AXE")
            WeaponType.ANY -> true
        }
    }

    override fun getDescription(): String {
        return when (type) {
            WeaponType.SWORD -> "剣が必要"
            WeaponType.WAND -> "杖が必要"
            WeaponType.AXE -> "斧が必要"
            WeaponType.ANY -> "武器が必要"
        }
    }
}

/** 特定のアイテムIDの要求 */
class SpecificItemRequirement(private val itemId: String) : SkillRequirement {
    override fun isMet(player: DungeonPlayer): Boolean {
        val item = player.player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return false
        val currentId =
                meta.persistentDataContainer.get(PluginKeys.ITEM_ID, PersistentDataType.STRING)
        return currentId == itemId
    }

    override fun getDescription(): String {
        return "専用武器が必要"
    }
}

/** 複合条件 (AND) */
class CompositeRequirement(private val requirements: List<SkillRequirement>) : SkillRequirement {
    override fun isMet(player: DungeonPlayer): Boolean {
        return requirements.all { it.isMet(player) }
    }

    override fun getDescription(): String {
        return requirements.joinToString(", ") { it.getDescription() }
    }
}

enum class WeaponType {
    SWORD,
    WAND,
    AXE,
    ANY
}
