package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

interface Dash : Skill {

    /**
     * 冲刺的距离.
     */
    val stepDistance: Double

    /**
     * 移动的 Tick 数
     */
    val duration: Long

    companion object Factory : SkillFactory<Dash> {
        override fun create(key: Key, config: ConfigProvider): Dash {
            val stepDistance = config.entry<Double>("step_distance")
            val duration = config.optionalEntry<Long>("duration").orElse(50)
            return DefaultImpl(key, config, stepDistance, duration)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        stepDistance: Provider<Double>,
        duration: Provider<Long>
    ) : Dash, SkillBase(key, config) {
        override val stepDistance: Double by stepDistance
        override val duration: Long by duration

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<Dash> {
            return DashTick(context, this, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }
    }
}

private class DashTick(
    context: SkillContext,
    skill: Dash,
    override val interruptTriggers: Provider<TriggerConditions>,
    override val forbiddenTriggers: Provider<TriggerConditions>
) : AbstractPlayerSkillTick<Dash>(skill, context) {

    override fun tickCastPoint(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("冲刺的前摇摇摇摇")
        return TickResult.ALL_DONE
    }

    override fun tickBackswing(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("冲刺的后摇摇摇摇摇摇摇")
        return TickResult.ALL_DONE
    }

    override fun tickCast(tickCount: Long): TickResult {
        if (tickCount >= skill.duration) {
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

        if (!blockInFront.isCanBeDashThrough()) {
            // 如果前方有方块，尝试向上移动一格高度
            val blockAboveFront = nextLocation.clone().add(0.0, 1.0, 0.0).block
            if (blockAboveFront.isCanBeDashThrough() && blockInFront.location.add(0.0, 1.0, 0.0).block.isCanBeDashThrough()) {
                stepVector = stepVector.setY(1.0)
            } else {
                return TickResult.ALL_DONE
            }
        } else {
            if (blockBelow.isCanBeDashThrough()) {
                // 如果脚下没有方块，尝试向下移动一格高度
                stepVector = stepVector.setY(-1.0)
            } else {
                // 保持原来的Y轴高度
                stepVector = stepVector.setY(0.0)
            }
        }

        // 应用速度到玩家对象上
        player.velocity = stepVector
        affectEntityNearby(player)

        return TickResult.CONTINUE_TICK
    }

    private fun affectEntityNearby(player: Player) {
        val entities = player.getNearbyEntities(2.0, 1.0, 2.0)
        for (entity in entities) {
            if (entity is LivingEntity) {
                entity.velocity = player.location.direction.multiply(2.0)
                entity.damage(2.0, player)
            }
        }
    }

    private fun Block.isCanBeDashThrough(): Boolean {
        return when {
            this.type == Material.AIR -> true
            this.isReplaceable -> true
            this.isLiquid -> true
            !this.isSolid -> true
            else -> false
        }
    }

}