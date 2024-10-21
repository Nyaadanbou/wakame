package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.util.NumberConversions
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    override fun handleDirectMeleeAttackEntity(player: Player, nekoStack: NekoStack, event: EntityDamageByEntityEvent): DamageMetadata? {
        val user = player.toUser()
        // 只要这个物品的内部冷却处于激活状态就不处理
        // 内部冷却不一定是攻速组件造成的
        if (user.attackSpeed.isActive(nekoStack.id)) {
            event.isCancelled = true
            return null
        }

        val attributeMap = user.attributeMap
        // 锤子直接命中的生物的伤害元数据
        val directDamageMetadata = PlayerDamageMetadata(
            damager = player,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.HAMMER),
            damageBundle = damageBundle(attributeMap) {
                every { standard() }
            }
        )
        val ratio = attributeMap.getValue(Attributes.HAMMER_DAMAGE_RATIO)
        // 锤子范围命中的生物的伤害元数据
        val extraDamageMetadata = PlayerDamageMetadata(
            damager = player,
            damageTags = DamageTags(DamageTag.MELEE, DamageTag.HAMMER, DamageTag.EXTRA),
            damageBundle = damageBundle(attributeMap) {
                every {
                    standard()
                    rate { ratio * standard() }
                }
            }
        )

        val damagee = event.entity
        val hitLocation = damagee.location
        val radius = attributeMap.getValue(Attributes.HAMMER_DAMAGE_RANGE).coerceAtLeast(0.0)
        // 范围伤害目标的判定为:
        // 以直接受击实体的脚部坐标向y轴正方向(上方)偏移0.5格为中心
        // 高为1格, 半径为radius的圆柱体
        hitLocation.clone().add(.0, .5, .0).getNearbyLivingEntities(radius, 0.5) {
            it.uniqueId != player.uniqueId && it.uniqueId != damagee.uniqueId && distanceXZ(it.location, hitLocation) < radius
        }.forEach {
            it.hurt(extraDamageMetadata, player, true)
        }

        // 攻击冷却
        nekoStack.applyAttackCooldown(player)
        // TODO 扣除耐久

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

        return directDamageMetadata
    }

    private fun distanceXZ(location1: Location, location2: Location): Double {
        if (location1.world != null && location2.world != null) {
            if (location1.world != location2.world) {
                throw IllegalArgumentException("Cannot measure distance between ${location1.world.name} and ${location2.world.name}")
            } else {
                return sqrt(NumberConversions.square(location1.x - location2.x) + NumberConversions.square(location1.z - location2.z))
            }
        } else {
            throw IllegalArgumentException("Cannot measure distance to a null world")
        }
    }
}