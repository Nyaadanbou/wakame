package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.util.getFirstBlockBelow
import cc.mewcraft.wakame.util.getTargetLocation
import cc.mewcraft.wakame.world.attribute.damage.CustomDamageMetaData
import cc.mewcraft.wakame.world.attribute.damage.ElementDamagePacket
import cc.mewcraft.wakame.world.attribute.damage.applyCustomDamage
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity

interface Lightning : Skill {

    val targetType: TargetType

    /**
     * 雷击允许的目标类型.
     */
    enum class TargetType {
        /**
         * 仅允许目标为实体.
         */
        ENTITY,

        /**
         * 仅允许目标为位置.
         */
        LOCATION,

        /**
         * 允许目标为实体或位置.
         */
        ALL
    }

    companion object Factory : SkillFactory<Lightning> {
        override fun create(key: Key, config: ConfigProvider): Lightning {
            val targetType = config.optionalEntry<TargetType>("target_type").orElse(TargetType.ALL)
            return DefaultImpl(key, config, targetType)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        targetType: Provider<TargetType>,
    ) : Lightning, SkillBase(key, config) {
        override val targetType: TargetType by targetType

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<Lightning> {
            return LightningTick(context, this, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }
    }
}

private class LightningTick(
    context: SkillContext,
    skill: Lightning,
    override val interruptTriggers: Provider<TriggerConditions>,
    override val forbiddenTriggers: Provider<TriggerConditions>
) : AbstractPlayerSkillTick<Lightning>(skill, context) {
    private val entityLocationTarget: Location?
        get() = TargetUtil.getLocation(context, true)?.bukkitLocation
    private val locationTarget: Location? = CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity?.location?.getTargetLocation(16)?.getFirstBlockBelow()?.location

    override fun tickCastPoint(tickCount: Long): TickResult {
        val target = getTargetLocation() ?: return TickResult.INTERRUPT
        if (tickCount >= 50) {
            return TickResult.ALL_DONE
        }
        generateBlueSmoke(target)

        return TickResult.CONTINUE_TICK
    }

    override fun tickCast(tickCount: Long): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val caster = CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity as? LivingEntity
        val target = getTargetLocation() ?: return TickResult.INTERRUPT
        val world = target.world
        world.strikeLightningEffect(target)
        val entitiesBeStruck = world.getNearbyEntities(target, 3.0, 3.0, 3.0)
        for (entity in entitiesBeStruck) {
            if (entity is LivingEntity) {
                entity.applyCustomDamage(
                    CustomDamageMetaData(
                        1.0, true,
                        listOf(ElementDamagePacket(ElementRegistry.DEFAULT, 5.0, 10.0, 0.0, 0.0, 0.0))
                    ),
                    caster
                )
            }
        }
        return TickResult.ALL_DONE
    }

    private fun generateBlueSmoke(location: Location) {
        ParticleBuilder(Particle.DUST)
            .count(50)
            .offset(0.5, 1.0, 0.5)
            .extra(0.5)
            .location(location)
            .allPlayers()
            .color(0, 127, 255)
            .spawn()
    }

    private fun getTargetLocation(): Location? {
        val targetType = skill.targetType
        return when (targetType) {
            Lightning.TargetType.ENTITY -> entityLocationTarget ?: return null
            Lightning.TargetType.LOCATION -> locationTarget ?: return null
            Lightning.TargetType.ALL -> entityLocationTarget ?: locationTarget ?: return null
        }
    }
}