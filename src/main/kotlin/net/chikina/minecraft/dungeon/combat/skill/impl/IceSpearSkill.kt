package net.chikina.minecraft.dungeon.combat.skill.impl

import net.chikina.minecraft.dungeon.combat.CombatEntity
import net.chikina.minecraft.dungeon.combat.DamageContext
import net.chikina.minecraft.dungeon.combat.DamageType
import net.chikina.minecraft.dungeon.combat.effect.FreezeEffect
import net.chikina.minecraft.dungeon.combat.skill.Skill
import net.chikina.minecraft.dungeon.item.GameMaterial
import net.chikina.minecraft.dungeon.util.PluginKeys
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class IceSpearSkill : Skill() {
  override val id: String = "ice_spear"
  override val name: String = "Ice Spear"
  override val cooldown: Long = 4000
  override val manaCost: Double = 15.0
  override val icon: ItemStack = ItemStack(Material.PACKED_ICE)
  override val unlockMaterial = GameMaterial.ORB_ICE

  override fun perform(attacker: CombatEntity, target: CombatEntity?) {
    val le = attacker.getLivingEntity() ?: return
    val arrow = le.launchProjectile(Arrow::class.java)

    arrow.velocity = le.location.direction.multiply(3.0)
    arrow.isCritical = true
    arrow.damage = 10.0

    arrow.persistentDataContainer.set(PluginKeys.skillId, PersistentDataType.STRING, id)
  }

  override fun onProjectileHit(event: ProjectileHitEvent, attacker: CombatEntity) {
    val entity = event.hitEntity ?: return
    val target = CombatEntity.from(entity) ?: return

    if (target == attacker) return

    val magicPower =
      attacker.stats.attack.baseAttack * (1.0 + attacker.stats.attack.magicAttack / 100.0)
    val context = DamageContext(15.0 + (magicPower * 1.2), DamageType.MAGIC, attacker, false)
    applyDamage(target, context)
    target.addEffect(FreezeEffect(3.0))
  }

  override fun getTargets(attacker: CombatEntity): List<CombatEntity> = emptyList()
}
