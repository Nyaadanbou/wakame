@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.util.krequire
import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type as ReflectType

interface Teleport : Skill {

    /**
     * 传送的类型.
     */
    val type: Type

    sealed interface Type {
        data class FIXED(val position: Position) : Type
        data class RANDOM(val distance: Double) : Type
        data object TARGET : Type
    }

    object TypeSerializer : SchemaSerializer<Type> {
        override fun deserialize(type: ReflectType, node: ConfigurationNode): Type {
            return when (val teleportType = node.node("type").krequire<String>()) {
                "fixed" -> {
                    Type.FIXED(
                        Position.block(
                            node.node("x").krequire<Int>(),
                            node.node("y").krequire<Int>(),
                            node.node("z").krequire<Int>()
                        )
                    )
                }

                "target" -> {
                    Type.TARGET
                }

                "random" -> {
                    Type.RANDOM(node.node("distance").krequire<Double>())
                }

                else -> {
                    throw IllegalArgumentException("Unknown teleportation type: $teleportType")
                }
            }
        }
    }

    companion object Factory : SkillFactory<Teleport> {
        override fun create(key: Key, config: ConfigProvider): Teleport {
            val type = config.optionalEntry<Type>("type").orElse(Type.FIXED(Position.block(0, 0, 0)))
            return DefaultImpl(key, config, type)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        type: Provider<Type>,
    ) : Teleport, SkillBase(key, config) {

        override val type: Type by type

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick {
            return Tick(context, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }

        private inner class Tick(
            context: SkillContext,
            override val interruptTriggers: TriggerConditions,
            override val forbiddenTriggers: TriggerConditions
        ) : AbstractPlayerSkillTick(this@DefaultImpl, context) {

            override fun tickCastPoint(tickCount: Long): TickResult {
                val player = context[SkillContextKey.CASTER]?.root<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("传送前摇")
                return TickResult.ALL_DONE
            }

            override fun tickBackswing(tickCount: Long): TickResult {
                val player = context[SkillContextKey.CASTER]?.root<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("传送后摇")
                return TickResult.ALL_DONE
            }

            override fun tickCast(tickCount: Long): TickResult {
                val node = context[SkillContextKey.CASTER] ?: return TickResult.INTERRUPT
                val caster = node.root<Caster.Single.Entity>() ?: return TickResult.INTERRUPT
                val location = TargetUtil.getLocation(context)?.bukkitLocation ?: return TickResult.INTERRUPT
                when (val type = type) {
                    is Type.FIXED -> {
                        val position = type.position
                        caster.bukkitEntity.teleport(position.toLocation(caster.bukkitEntity.world))
                    }

                    is Type.RANDOM -> {
                        val random = location.clone().add(
                            (Math.random() - 0.5) * type.distance,
                            0.0,
                            (Math.random() - 0.5) * type.distance
                        )
                        caster.bukkitEntity.teleport(random)
                    }

                    is Type.TARGET -> {
                        val bukkitLocation = location.apply {
                            pitch = caster.bukkitEntity.location.pitch
                            yaw = caster.bukkitEntity.location.yaw
                        }
                        caster.bukkitEntity.teleport(bukkitLocation)
                    }
                }
                return TickResult.ALL_DONE
            }
        }
    }
}
