package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.key.Key
import org.bukkit.Particle

interface Lightning : Skill {

    companion object Factory : SkillFactory<Lightning> {
        override fun create(key: Key, config: ConfigProvider): Lightning {
            return DefaultImpl(key, config)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
    ) : Lightning, SkillBase(key, config) {
        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick {
            return LightningTick(context, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }

        private inner class LightningTick(
            context: SkillContext,
            override val interruptTriggers: Provider<TriggerConditions>,
            override val forbiddenTriggers: Provider<TriggerConditions>
        ) : AbstractPlayerSkillTick(this@DefaultImpl, context) {
            private val caster: Caster.Single.Player? = CasterUtil.getCaster<Caster.Single.Player>(context)

            override fun tickCastPoint(tickCount: Long): TickResult {
                if (tickCount >= 50) {
                    return TickResult.ALL_DONE
                }
                val location = TargetUtil.getLocation(context)
                location?.let { generateBlueSmoke(it) }

                return TickResult.CONTINUE_TICK
            }

            override fun tickCast(tickCount: Long): TickResult {
                val location = TargetUtil.getLocation(context)?.bukkitLocation ?: return TickResult.INTERRUPT
                val world = location.world
                val lightning = world.strikeLightning(location)
                return TickResult.ALL_DONE
            }

            override fun tickBackswing(tickCount: Long): TickResult {
                if (tickCount >= 5) {
                    caster ?: return TickResult.ALL_DONE
                    ParticleBuilder(Particle.SONIC_BOOM)
                        .count(1)
                        .offset(0.5, 1.0, 0.5)
                        .extra(0.5)
                        .allPlayers()
                        .location(caster.bukkitPlayer.location)
                        .spawn()

                    return TickResult.ALL_DONE
                }
                return TickResult.CONTINUE_TICK
            }

            private fun generateBlueSmoke(location: Target.Location) {
                ParticleBuilder(Particle.DUST)
                    .count(50)
                    .offset(0.5, 1.0, 0.5)
                    .extra(0.5)
                    .location(location.bukkitLocation)
                    .allPlayers()
                    .color(0, 0, 255)
                    .spawn()
            }
        }
    }
}