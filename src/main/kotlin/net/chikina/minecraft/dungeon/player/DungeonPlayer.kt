package net.chikina.minecraft.dungeon.player

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.MagicElement
import net.chikina.minecraft.dungeon.combat.effect.VanillaEffect
import net.chikina.minecraft.dungeon.combat.skill.SkillRegistry
import net.chikina.minecraft.dungeon.combat.skill.SkillSlot
import net.chikina.minecraft.dungeon.enemy.DungeonEnemy
import net.chikina.minecraft.dungeon.item.ArmorItem
import net.chikina.minecraft.dungeon.item.GameItem
import net.chikina.minecraft.dungeon.item.ItemAttribute
import net.chikina.minecraft.dungeon.item.WandItem
import net.chikina.minecraft.dungeon.item.WeaponItem
import net.chikina.minecraft.dungeon.mining.MiningEntity
import net.chikina.minecraft.dungeon.mining.PlayerMiningEntity
import net.chikina.minecraft.dungeon.stats.CombatStats
import net.chikina.minecraft.dungeon.stats.DefenseStats
import net.chikina.minecraft.dungeon.stats.StatCalculator
import net.chikina.minecraft.dungeon.ui.Sidebar
import net.chikina.minecraft.dungeon.util.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import java.util.LinkedList

class DungeonPlayer(
  var player: Player,
) : CombatEntity() {
  override val name: String
    get() = player.name
  override val location: Location
    get() = player.location
  override val stats: CombatStats = CombatStats()
  override val level: Int
    get() = playerData.level

  var currentMana: Double = 0.0

  val miningEntity: MiningEntity = PlayerMiningEntity(this)

  var playerData: PlayerData = PlayerData(player.uniqueId)

  init {
    initStats()
  }

  override fun onDamageTaken(context: DamageContext) {
    syncVisualHealth()
  }

  override fun onHealed(amount: Double) {
    syncVisualHealth()
  }

  private fun syncVisualHealth() {
    if (stats.hp > 0) {
      val visualMaxHp = 20.0

      @Suppress("DEPRECATION")
      run {
        val attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
        if (attr != null) {
          attr.baseValue = visualMaxHp
        }
      }

      if (currentHp > stats.hp) currentHp = stats.hp

      val visualHp =
        if (currentHp > 0) {
          ((currentHp / stats.hp) * visualMaxHp).coerceIn(0.5, visualMaxHp)
        } else {
          0.0
        }

      runCatching { player.health = visualHp }
    }
  }

  fun initStats() {
    resetBaseStats()
    applyDefaultEffects()
  }

  private fun resetBaseStats() {
    stats.hp = 20.0
    stats.maxMana = 100.0
    // currentMana = stats.maxMana // Removed to prevent reset on weapon swap
    stats.attack.baseAttack = 1.0
    stats.attack.physicalAttack = 100.0
    stats.attack.magicAttack = 100.0
    stats.attack.fireAttack = 100.0
    stats.attack.waterAttack = 100.0
    stats.attack.thunderAttack = 100.0
    stats.attack.windAttack = 100.0
    stats.attack.earthAttack = 100.0
    stats.critRate = 5.0
    stats.critDamage = 150.0
    stats.itemDropRate = 0.0
    stats.maxPoise = 0.0
    stats.poiseRegen = 0.0
    stats.defense = DefenseStats()
  }

  fun checkAndConsumeMana(amount: Double): Boolean {
    if (amount <= 0) return true
    if (currentMana < amount) {
      player.sendActionBar(
        Component.text(
          "マナが足りません! (${currentMana.toInt()}/${amount.toInt()})",
          NamedTextColor.RED,
        ),
      )
      return false
    }
    currentMana -= amount
    Sidebar.update(player)
    return true
  }

  fun castSkill(skillId: String, itemOverride: ItemStack? = null): Boolean {
    val skill = SkillRegistry.get(skillId) ?: return false

    if (!checkDependency(skill, itemOverride)) return false
    if (!checkCooldown(skill)) return false
    if (!checkAndConsumeMana(skill.manaCost)) return false

    skill.perform(this, null)
    skillCooldowns[skillId] = System.currentTimeMillis()
    return true
  }

  private fun checkDependency(
    skill: net.chikina.minecraft.dungeon.combat.skill.Skill,
    itemOverride: ItemStack?,
  ): Boolean {
    if (skill.dependency?.isMet(this, itemOverride) != false) return true

    sendRequirementError(skill, itemOverride)
    return false
  }

  private fun sendRequirementError(
    skill: net.chikina.minecraft.dungeon.combat.skill.Skill,
    itemOverride: ItemStack?,
  ) {
    val item = itemOverride ?: player.inventory.itemInMainHand
    val gameItem = GameItem(item)
    val attrs = gameItem.getAttributes()
    player.sendMessage(
      Component.text(
        "発動条件を満たしていません: ${skill.dependency?.getDescription()} (Item: ${item.type}, Attrs: $attrs)",
        NamedTextColor.RED,
      ),
    )
  }

  private fun checkCooldown(skill: net.chikina.minecraft.dungeon.combat.skill.Skill): Boolean {
    val remaining = getRemainingCooldown(skill)
    if (remaining <= 0) return true

    sendCooldownError(remaining)
    return false
  }

  private fun getRemainingCooldown(
    skill: net.chikina.minecraft.dungeon.combat.skill.Skill,
  ): Double {
    val lastUse = skillCooldowns[skill.id] ?: return 0.0
    val now = System.currentTimeMillis()
    val elapsed = now - lastUse
    if (elapsed >= skill.cooldown) return 0.0
    return (skill.cooldown - elapsed) / 1000.0
  }

  private fun sendCooldownError(remaining: Double) {
    val formatted = String.format("%.1f", remaining)
    player.sendMessage(Component.text("クールダウン中: ${formatted}s", NamedTextColor.RED))
  }

  private fun applyDefaultEffects() {
    super.addEffect(
      VanillaEffect(
        PotionEffectType.NIGHT_VISION,
        Int.MAX_VALUE,
        255,
        false,
        false,
        false,
        isPersistent = true,
      ),
    )
    super.addEffect(
      VanillaEffect(
        PotionEffectType.HASTE,
        Int.MAX_VALUE,
        255,
        false,
        false,
        false,
        isPersistent = true,
      ),
    )
    super.addEffect(
      VanillaEffect(
        PotionEffectType.MINING_FATIGUE,
        Int.MAX_VALUE,
        255,
        false,
        false,
        false,
        isPersistent = true,
      ),
    )
    super.addEffect(
      VanillaEffect(
        PotionEffectType.SATURATION,
        Int.MAX_VALUE,
        255,
        false,
        false,
        false,
        isPersistent = true,
      ),
    )
  }

  fun updateStats() {
    resetBaseStats()
    StatCalculator.calculateCombatStats(playerData, stats)

    val weaponItem = WeaponItem(GameItem(player.inventory.itemInMainHand).itemStack)

    stats.attack.baseAttack += weaponItem.attackDamage

    val wand = WandItem(player.inventory.itemInMainHand)
    val element = wand.element
    if (element != null) {
      when (element) {
        MagicElement.FIRE -> stats.attack.fireAttack += 50.0
        MagicElement.WATER -> stats.attack.waterAttack += 50.0
        MagicElement.THUNDER -> stats.attack.thunderAttack += 50.0
        MagicElement.WIND -> stats.attack.windAttack += 50.0
        MagicElement.EARTH -> stats.attack.earthAttack += 50.0
      }
    }

    // Apply Armor Stats
    val equipment = player.inventory.armorContents
    for (item in equipment) {
      if (item == null || item.type.isAir) continue
      val gameItem = GameItem(item)
      if (gameItem.hasAttribute(ItemAttribute.ARMOR)) {
        val armor = ArmorItem(item)
        stats.hp += armor.hp
        stats.defense.baseDefense += armor.defense

        // Endurance from armor increases Poise
        // 30 base + (End * 2) -> Additive
        // Recalculate Poise addition from armor endurance
        // StatCalculator already handles BASE stats from allocation.
        // We just add extra from armor here.
        val armorEnd = armor.endurance
        if (armorEnd > 0) {
          // Adding directly to stats or treating as bonus?
          // stats.maxPoise += armorEnd * 2.0
          // stats.defense.baseDefense += armorEnd * 1.0 (Def per End) - Usually handled
          // by calculator but this is extra
          // Let's assume Armor stats are FLAT bonuses on top of attributes.
          // But ArmorItem definition has `defense` and `endurance`.
          // `defense` is the base defense of the item. `endurance` is the stat provided.
          // If it provides endurance, it should logically provide Defense AND Poise via
          // the attribute scaling?
          // Or just provide raw stats?
          // The user asked "ENDURANCEを強化したら強靭値が強化される".
          // So if armor gives +Endurance, it should give +Defense and +Poise.
          // For simplicity in `ArmorItem`, I defined `defense` and `endurance`
          // separately.
          // Let's treat them as:
          // `defense` property = Flat Defense Bonus
          // `endurance` property = Flat Endurance Stat Bonus

          // Apply Endurance effects:
          stats.defense.baseDefense += armorEnd * 1.0 // DEF_PER_END
          stats.maxPoise += armorEnd * 2.0 // POISE_PER_END
        }
      }
    }

    miningEntity.updateStats()

    if (stats.hp <= 0.0) stats.hp = 1.0
    if (currentHp > stats.hp) currentHp = stats.hp
    if (currentMana > stats.maxMana) currentMana = stats.maxMana

    syncVisualHealth()
  }

  fun regenerateMana() {
    if (currentMana < stats.maxMana) {
      currentMana = (currentMana + stats.manaRegen).coerceAtMost(stats.maxMana)
    }
  }

  fun regenerateHealth() {
    if (currentHp < stats.hp && stats.hp > 0) {
      currentHp = (currentHp + stats.hpRegen).coerceAtMost(stats.hp)
      syncVisualHealth()
    }
  }

  override fun getLivingEntity(): LivingEntity = player

  override fun getTarget(): CombatEntity? {
    val target = player.getTargetEntity(3) ?: return null
    return CombatEntity.from(target)
  }

  override fun sendMessage(message: String) {
    player.sendMessage(message)
  }

  override fun sendMessage(message: Component) {
    player.sendMessage(message)
  }

  override fun isAlly(other: CombatEntity): Boolean = other is DungeonPlayer

  override fun consumeMana(amount: Double): Boolean = checkAndConsumeMana(amount)

  override fun onKill(victim: CombatEntity) {
    if (victim is DungeonEnemy) {
      val multiplier = 1.0 + (stats.itemDropRate / 100.0)
      val runeAmount = (victim.baseRunes * multiplier).toLong()

      playerData.accumulatedRunes += runeAmount
      Messenger.send(player, Component.text("+ $runeAmount ルーン", NamedTextColor.AQUA))
      Sidebar.update(player)
    }
  }

  override fun notifyRareDrop(
    dropName: Component,
    originalChance: Double,
    finalChance: Double,
    itemStack: ItemStack,
  ) {
    val basePct = String.format("%.1f", originalChance * 100)
    val finalPct = String.format("%.1f", finalChance * 100)

    Messenger.send(
      player,
      Component
        .text("レアドロップ！ ", NamedTextColor.GOLD)
        .append(dropName)
        .append(
          Component.text(
            " がドロップしました: $basePct% ($finalPct%)",
            NamedTextColor.YELLOW,
          ),
        ),
    )
  }

  fun extractRunes(rate: Double): Long {
    val acc = playerData.accumulatedRunes
    if (acc <= 0) return 0

    val extracted = (acc * rate).toLong()
    playerData.runes += extracted
    playerData.accumulatedRunes = 0
    return extracted
  }

  data class SoldItem(
    val itemStack: ItemStack,
    val soldPrice: Long,
  )

  val soldItems = LinkedList<SoldItem>()

  fun addSoldItem(item: ItemStack, price: Long) {
    soldItems.addFirst(SoldItem(item, price))
    if (soldItems.size > MAX_BUYBACK_ITEMS) {
      soldItems.removeLast()
    }
  }

  companion object {
    private const val MAX_BUYBACK_ITEMS = 40
  }

  val skillCooldowns = mutableMapOf<String, Long>()

  fun handleInput(action: ActionType, target: CombatEntity? = null): Boolean {
    if (player.isSneaking) {
      val slot =
        when (action) {
          ActionType.LEFT_CLICK -> SkillSlot.SHIFT_LEFT_CLICK
          ActionType.RIGHT_CLICK -> SkillSlot.SHIFT_RIGHT_CLICK
        }

      val skillId = playerData.equippedSkills[slot]
      if (skillId != null && skillId.isNotEmpty()) {
        castSkill(skillId)
        return true
      }
    }

    return false
  }

  enum class ActionType {
    LEFT_CLICK,
    RIGHT_CLICK,
  }
}
