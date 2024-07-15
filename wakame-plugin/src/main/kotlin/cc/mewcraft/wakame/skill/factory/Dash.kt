package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import net.kyori.adventure.key.Key
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface Dash : Skill {

    /**
     * 冲刺的距离.
     */
    val distance: Double

    companion object Factory : SkillFactory<Dash> {
        override fun create(key: Key, config: ConfigProvider): Dash {
            val distance = config.entry<Double>("distance")
            return DefaultImpl(key, config, distance)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        distance: Provider<Double>,
    ) : Dash, SkillBase(key, config) {

        override val distance: Double by distance

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
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT

        val location = player.location
        val velocity = location.direction.normalize().setY(0).multiply(skill.distance)
        player.velocity = velocity

        val entities = getEntitiesInPath(player, location, velocity)
        entities.forEach { it.damage(114.0, player) }

        return TickResult.ALL_DONE
    }

    private fun getEntitiesInPath(player: Player, start: Location, direction: Vector): List<LivingEntity> {
        // 创建一个空的实体列表，用于存储路径上的生物
        val entitiesOnPath = mutableListOf<LivingEntity>()

        // 射线追踪
        val rayTraceResult = start.world.rayTrace(
            start,
            direction,
            skill.distance,
            FluidCollisionMode.NEVER,
            false,
            1.0
        ) { entity ->
            entity != player && entity is LivingEntity // 排除玩家自身，并且只考虑生物实体
        }

        // 如果射线追踪命中，则将命中的实体添加到列表中
        rayTraceResult?.hitEntity?.let { entity ->
            if (entity is LivingEntity) {
                entitiesOnPath.add(entity)
            }
        }

        return entitiesOnPath
    }
}