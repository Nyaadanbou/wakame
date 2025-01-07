package cc.mewcraft.wakame.ability.factory.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.AbilityMechanic
import cc.mewcraft.wakame.ability.AbilityProvider
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.ability.factory.AbilityFactory
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

/**
 * 冲刺技能.
 */
interface Dash : Ability {

    /**
     * 冲刺的距离.
     */
    val stepDistance: Double

    /**
     * 移动的 Tick 数
     */
    val duration: Long

    /**
     * 撞到实体后是否能继续冲刺
     */
    val canContinueAfterHit: Boolean

    /**
     * 撞到实体后触发的效果
     */
    val hitEffects: List<AbilityProvider>

    /**
     * 判定的间隔.
     */
    val hitInterval: Long

    companion object Factory : AbilityFactory<Dash> {
        override fun create(key: Key, config: ConfigurationNode): Dash {
            val stepDistance = config.node("step_distance").require<Double>()
            val duration = config.node("duration").get<Long>() ?: 50
            val canContinueAfterHit = config.node("can_continue_after_hit").get<Boolean>() ?: true
            val hitEffect = config.node("hit_effects").get<List<AbilityProvider>>() ?: emptyList()
            val hitInterval = config.node("hit_interval").get<Long>() ?: 20
            return Impl(key, config, stepDistance, duration, canContinueAfterHit, hitEffect, hitInterval)
        }
    }

    private class Impl(
        key: Key,
        config: ConfigurationNode,
        override val stepDistance: Double,
        override val duration: Long,
        override val canContinueAfterHit: Boolean,
        override val hitEffects: List<AbilityProvider>,
        override val hitInterval: Long,
    ) : Dash, AbilityBase(key, config) {
        override fun mechanic(input: AbilityInput): AbilityMechanic {
            return DashAbilityMechanic(this)
        }
    }
}

private class DashAbilityMechanic(
    private val ability: Dash,
) : AbilityMechanic() {

    companion object {
        /**
         * 在 Dash 开始前的准备时间
         */
        private const val STARTING_TICK: Long = 10L
    }

    override fun tickCastPoint(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        return TickResult.NEXT_STATE
    }

    override fun tickCast(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        if (tickCount >= ability.duration + STARTING_TICK) {
            // 超过了执行时间, 直接完成技能
            return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
        }
        val entity = componentMap.castByEntity() ?: return@abilitySupport TickResult.INTERRUPT // 无效生物
        val direction = entity.location.direction.setY(0).normalize()
        val stepDistance = ability.stepDistance
        // 计算每一步的移动向量
        var stepVector = direction.clone().multiply(stepDistance)
        // 检查前方和脚下的方块
        val nextLocation = entity.location.add(stepVector)
        val blockInFront = nextLocation.block
        val blockBelow = nextLocation.clone().add(0.0, -1.0, 0.0).block

        if (!blockInFront.isAccessible()) {
            // 如果前方有方块，尝试向上移动一格高度
            val blockAboveFront = nextLocation.clone().add(0.0, 1.0, 0.0).block
            if (blockAboveFront.isAccessible() && blockInFront.location.add(0.0, 1.0, 0.0).block.isAccessible()) {
                stepVector = stepVector.setY(1.0)
            } else {
                return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
            }
        } else {
            stepVector = if (blockBelow.isAccessible()) {
                // 如果脚下没有方块，尝试向下移动一格高度
                stepVector.setY(-1.0)
            } else {
                // 保持原来的Y轴高度
                stepVector.setY(0.0)
            }
        }

        // 应用速度到玩家对象上
        entity.velocity = stepVector

        if (affectEntityNearby(entity)) {
            if (!ability.canContinueAfterHit) {
                return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
            }
        }

        return@abilitySupport TickResult.CONTINUE_TICK
    }

    private fun affectEntityNearby(casterEntity: Entity): Boolean {
        val entities = casterEntity.getNearbyEntities(2.0, 1.0, 2.0)
        if (entities.isEmpty()) {
            return false
        }
        for (entity in entities) {
            if (entity !is LivingEntity)
                continue

            for (abilityProvider in ability.hitEffects) {
                val effect = abilityProvider.get()
                val input = abilityInput(CasterAdapter.adapt(casterEntity)) {
                    target(TargetAdapter.adapt(entity))
                }
                effect.recordBy(input)
            }
        }
        return true
    }

    private fun Block.isAccessible(): Boolean {
        return when {
            this.type == Material.AIR -> true
            this.isReplaceable -> true
            this.isLiquid -> true
            !this.isSolid -> true
            else -> false
        }
    }
}