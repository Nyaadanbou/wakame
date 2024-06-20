@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
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

        override fun cast(context: SkillCastContext): SkillTick {
            return Tick(context)
        }

        private inner class Tick(
            context: SkillCastContext,
        ) : PlayerSkillTick(this@DefaultImpl, context) {

            override fun tickCastPoint(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("传送前摇")
                return TickResult.ALL_DONE
            }

            override fun tickBackswing(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("传送后摇")
                return TickResult.ALL_DONE
            }

            override fun tickCast(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER) ?: return TickResult.INTERRUPT
                val location = context.optional(SkillCastContextKey.TARGET_LOCATION)?.bukkitLocation ?: return TickResult.INTERRUPT
                when (val type = type) {
                    is Type.FIXED -> {
                        val position = type.position
                        player.bukkitPlayer.teleport(position.toLocation(player.bukkitPlayer.world))
                    }

                    is Type.TARGET -> {
                        val bukkitLocation = location.apply {
                            pitch = player.bukkitPlayer.location.pitch
                            yaw = player.bukkitPlayer.location.yaw
                        }
                        player.bukkitPlayer.teleport(bukkitLocation)
                    }
                }
                return TickResult.ALL_DONE
            }
        }
    }
}
