package cc.mewcraft.wakame.skill2.factory.implement

import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.SkillProvider
import cc.mewcraft.wakame.skill2.context.SkillContext
import cc.mewcraft.wakame.skill2.context.toMutable
import cc.mewcraft.wakame.skill2.factory.SkillFactory
import cc.mewcraft.wakame.skill2.result.SkillResult
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

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
        override val hitInterval: Long,
    ) : Dash, SkillBase(key, config) {
        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun result(context: SkillContext): SkillResult<Dash> {
            val newContext = context.toMutable().also { it.setSkill(this)  }
            return DashSkillResult(newContext, this)
        }
    }
}

private class DashSkillResult(
    override val context: SkillContext,
    private val skill: Dash
) : SkillResult<Dash> {

    override fun tickCast(tickCount: Double, componentMap: ComponentMap) {
        val bukkitEntity = componentMap[skill.key.asString(), BukkitEntityComponent]?.entity ?: return

        bukkitEntity.sendPlainMessage("Dash, tickCount: $tickCount")
    }
}