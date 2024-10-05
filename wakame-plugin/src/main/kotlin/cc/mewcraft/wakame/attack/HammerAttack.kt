package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 自定义锤攻击.
 * 对直接攻击到的实体造成面板伤害.
 * 对 锤击范围 内的实体造成 锤击威力*面板 的伤害.
 * 锤击范围由属性 [cc.mewcraft.wakame.attribute.Attributes.HAMMER_DAMAGE_RANGE] 决定.
 * 锤击威力由属性 [cc.mewcraft.wakame.attribute.Attributes.HAMMER_DAMAGE_RATIO] 决定.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: hammer
 * ```
 */
class HammerAttack : AttackType {
    companion object {
        const val NAME = "hammer"
    }

    override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
        val damageMetadata = event.damageMetadata
        val damageTags = damageMetadata.damageTags
        if (damageTags.contains(DamageTag.HAMMER) && !damageTags.contains(DamageTag.EXTRA)) {
            val extraDamageBundle = DamageBundle()
            val attributeMap = player.toUser().attributeMap
            damageMetadata.damageBundle.packets().forEach {
                extraDamageBundle.add(
                    DamagePacket(
                        element = it.element,
                        min = it.min,
                        max = it.max,
                        rate = it.rate * attributeMap.getValue(Attributes.HAMMER_DAMAGE_RATIO),
                        defensePenetration = it.defensePenetration,
                        defensePenetrationRate = it.defensePenetrationRate
                    )
                )
            }
            val customDamageMetadata = CustomDamageMetadata(
                criticalPower = damageMetadata.criticalPower,
                criticalState = damageMetadata.criticalState,
                knockback = true,
                damageBundle = extraDamageBundle,
                damageTags = DamageTags(DamageTag.MELEE, DamageTag.HAMMER, DamageTag.EXTRA)
            )
            val hitLocation = damagee.location
            val radius = attributeMap.getValue(Attributes.HAMMER_DAMAGE_RANGE).coerceAtLeast(0.0)
            hitLocation.getNearbyLivingEntities(radius).forEach {
                if (it.uniqueId != player.uniqueId && it.uniqueId != damagee.uniqueId) {
                    it.hurt(customDamageMetadata, player)
                }
            }

            // 特效
            val world = player.world
            val n = 16
            val angleIncrement = 2 * PI / n
            val blockData = hitLocation.clone().add(.0, -1.0, .0).block.blockData
            for (i in 0 until n) {
                val angle = i * angleIncrement
                val x = hitLocation.x + radius * cos(angle)
                val z = hitLocation.z + radius * sin(angle)
                ParticleBuilder(Particle.BLOCK)
                    .data(blockData)
                    .location(world, x, hitLocation.y, z)
                    .count(10)
                    .offset(0.3, 0.1, 0.3)
                    .extra(0.15)
                    .allPlayers()
                    .spawn()
            }
            ParticleBuilder(Particle.BLOCK)
                .data(blockData)
                .location(hitLocation)
                .count(50)
                .offset(0.3, 0.3, 0.3)
                .extra(0.15)
                .allPlayers()
                .spawn()
            world.playSound(player.location, Sound.ITEM_MACE_SMASH_GROUND, SoundCategory.PLAYERS, 1F, 1F)
        }
    }
}