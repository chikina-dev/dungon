package net.chikina.minecraft.dungeon.stats

/** ダンジョンシステム内の全てのステータスを保持するコンテナクラス。 */
data class CombatStats(
        var hp: Double = 0.0,
        var hpRegen: Double = 0.0, // 割合
        var maxMana: Double = 0.0,
        var manaRegen: Double = 0.0, // 固定回復量 or 割合 (後で調整)
        var attack: AttackStats = AttackStats(),
        var defense: DefenseStats = DefenseStats(),
        var attackSpeed: Double = 0.0,
        var critRate: Double = 0.0, // 割合
        var critDamage: Double = 0.0, // 割合
        var speed: Double = 0.0,
        var evasion: EvasionStats = EvasionStats(),
        var reflection: Double = 0.0,
        var lifeSteal: Double = 0.0, // 割合
        var bonus: BonusStats = BonusStats(),
        var itemDropRate: Double = 0.0, // 割合
        var faith: Double = 0.0 // 信仰値
)

/** 採掘に関連するステータス。 */
data class MiningStats(
        var speed: Int = 100, // 採掘速度
        var breakingPower: Int = 1, // 破壊力
        var fortune: Int = 100 // 採掘運 (割合)
)

/** 攻撃に関連するステータス。 基礎攻撃力は実数、その他は割合（通常0～100、またはそれ以上）。 注: プレイヤーは通常、属性攻撃力が100%から始まりますが、敵は0%です。 */
data class AttackStats(
        var baseAttack: Double = 0.0,
        var physicalAttack: Double = 0.0, // 割合
        var magicAttack: Double = 0.0, // 割合
        var fireAttack: Double = 0.0, // 割合
        var waterAttack: Double = 0.0, // 割合
        var thunderAttack: Double = 0.0, // 割合
        var windAttack: Double = 0.0, // 割合
        var earthAttack: Double = 0.0 // 割合
)

/** 防御に関連するステータス。 基礎防御力は実数、その他は割合。 */
data class DefenseStats(
        var baseDefense: Double = 0.0,
        var critDefense: Double = 0.0, // 割合
        var damageReduction: Double = 0.0, // 割合 (一般的なダメージ軽減)
        var physicalDefense: Double = 0.0, // 割合
        var magicDefense: Double = 0.0, // 割合
        var fireDefense: Double = 0.0, // 割合
        var waterDefense: Double = 0.0, // 割合
        var thunderDefense: Double = 0.0, // 割合
        var windDefense: Double = 0.0, // 割合
        var earthDefense: Double = 0.0 // 割合
)

/** 経験値やゴールド獲得などのボーナスステータス。 */
data class BonusStats(
        var stone: Double = 0.0, // 割合
        var exp: Double = 0.0, // 割合
        var gold: Double = 0.0 // 割合
)

/** 回避ステータス。 */
data class EvasionStats(
        var chance: Double = 0.0 // 割合
)
