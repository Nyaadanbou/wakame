package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

const val STARTING_TICK: Long = 10L

interface Dash : Skill {

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
    val hitEffects: List<SkillProvider>

    /**
     * 判定的间隔.
     */
    val hitInterval: Long

    companion object Factory : SkillFactory<Dash> {
        override fun create(key: Key, config: ConfigurationNode): Dash {
            val stepDistance = config.node("step_distance").krequire<Double>()
            val duration = config.node("duration").get<Long>() ?: 50
            val canContinueAfterHit = config.node("can_continue_after_hit").get<Boolean>() ?: true
            val hitEffect = config.node("hit_effects").get<List<SkillProvider>>() ?: emptyList()
            val hitInterval = config.node("hit_interval").get<Long>() ?: 20
            return Impl(key, config, stepDistance, duration, canContinueAfterHit, hitEffect, hitInterval)
        }
    }

    private class Impl(
        override val key: Key,
        config: ConfigurationNode,
        override val stepDistance: Double,
        override val duration: Long,
        override val canContinueAfterHit: Boolean,
        override val hitEffects: List<SkillProvider>,
        override val hitInterval: Long
    ) : Dash, SkillBase(key, config) {

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<Dash> {
            return DashTick(context, this, triggerConditionGetter.interrupt, triggerConditionGetter.forbidden)
        }
    }
}

private class DashTick(
    context: SkillContext,
    skill: Dash,
    override val interruptTriggers: TriggerConditions,
    override val forbiddenTriggers: TriggerConditions
) : AbstractPlayerSkillTick<Dash>(skill, context) {
    companion object {
        private val DASH_DAMAGE_KEY: SkillContextKey<Int> = SkillContextKey.create("dash_damage")
        private val DASH_EFFECT_TIME: SkillContextKey<Long> = SkillContextKey.create("dash_effect_time")
    }

    override fun tickCast(tickCount: Long): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        if (tickCount >= skill.duration + STARTING_TICK) {
            return TickResult.ALL_DONE
        }

        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        val direction = player.location.direction.setY(0).normalize()

        val stepDistance = skill.stepDistance
        // 计算每一步的移动向量
        var stepVector = direction.clone().multiply(stepDistance)

        // 检查前方和脚下的方块
        val nextLocation = player.location.add(stepVector)
        val blockInFront = nextLocation.block
        val blockBelow = nextLocation.clone().add(0.0, -1.0, 0.0).block

        if (!blockInFront.isAccessibleForDash()) {
            // 如果前方有方块，尝试向上移动一格高度
            val blockAboveFront = nextLocation.clone().add(0.0, 1.0, 0.0).block
            if (blockAboveFront.isAccessibleForDash() && blockInFront.location.add(0.0, 1.0, 0.0).block.isAccessibleForDash()) {
                stepVector = stepVector.setY(1.0)
            } else {
                return TickResult.ALL_DONE
            }
        } else {
            stepVector = if (blockBelow.isAccessibleForDash()) {
                // 如果脚下没有方块，尝试向下移动一格高度
                stepVector.setY(-1.0)
            } else {
                // 保持原来的Y轴高度
                stepVector.setY(0.0)
            }
        }

        // 应用速度到玩家对象上
        player.velocity = stepVector
        player.isSprinting = true
        if (tickCount >= STARTING_TICK && !player.hasActiveItem()) {
            return TickResult.ALL_DONE
        }

        if (affectEntityNearby(player)) {
            context[DASH_DAMAGE_KEY] = context[DASH_DAMAGE_KEY]?.plus(1) ?: 1
            if (!skill.canContinueAfterHit) {
                return TickResult.ALL_DONE
            }
        }

        return TickResult.CONTINUE_TICK
    }

    override fun tickBackswing(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer
        player?.isSprinting = false
        val damageCount = context[DASH_DAMAGE_KEY] ?: return TickResult.ALL_DONE
        context[SkillContextKey.NEKO_STACK]?.let {
            val components = it.components
            val damage = components.get(ItemComponentTypes.DAMAGEABLE) ?: return@let
            if (damage.damage < damage.maxDamage) {
                components.set(ItemComponentTypes.DAMAGEABLE, Damageable(damage.damage + damageCount, damage.maxDamage))
            } else {
                return TickResult.ALL_DONE
            }
        }
        return TickResult.ALL_DONE
    }

    private fun affectEntityNearby(livingEntity: LivingEntity): Boolean {
        val entities = livingEntity.getNearbyEntities(2.0, 1.0, 2.0)
        if (entities.isEmpty()) {
            return false
        }
        for (entity in entities) {
            if (entity !is LivingEntity)
                continue
            if (context[DASH_EFFECT_TIME]?.let { DashSupport.server.currentTick - it < skill.hitInterval } == true)
                continue

            for (skillProvider in skill.hitEffects) {
                val effect = skillProvider.get()
                val newContext = SkillContext(CasterAdapter.adapt(livingEntity), TargetAdapter.adapt(entity))
                Ticker.INSTANCE.schedule(effect.cast(newContext))
                context[DASH_EFFECT_TIME] = DashSupport.server.currentTick.toLong()
            }
        }
        return true
    }

    private fun Block.isAccessibleForDash(): Boolean {
        return when {
            this.type == Material.AIR -> true
            this.isReplaceable -> true
            this.isLiquid -> true
            !this.isSolid -> true
            else -> false
        }
    }

}

private object DashSupport : KoinComponent {
    val server: Server by inject()
}