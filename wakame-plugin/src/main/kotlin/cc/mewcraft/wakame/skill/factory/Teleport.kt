@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.factory.Teleport.Type
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
        data class RANDOM(
            val minDistance: Int,
            val maxDistance: Int,
        ) : Type

        data object TARGET : Type
    }

    companion object Factory : SkillFactory<Teleport> {
        override fun create(key: Key, config: ConfigurationNode): Teleport {
            val type = config.node("teleportation").krequire<Type>()
            return DefaultImpl(key, config, type)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigurationNode,
        override val type: Type,
    ) : Teleport, SkillBase(key, config) {

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<Teleport> {
            return TeleportTick(context, this, triggerConditionGetter.interrupt, triggerConditionGetter.forbidden)
        }
    }
}

private class TeleportTick(
    context: SkillContext,
    skill: Teleport,
    override val interruptTriggers: TriggerConditions,
    override val forbiddenTriggers: TriggerConditions
) : AbstractPlayerSkillTick<Teleport>(skill, context) {

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
        if (!checkConditions())
            return TickResult.ALL_DONE
        val caster = CasterUtils.getCaster<Caster.Single.Player>(context) ?: return TickResult.INTERRUPT
        val location = TargetUtil.getLocation(context)?.bukkitLocation ?: return TickResult.INTERRUPT
        when (val type = skill.type) {
            is Type.FIXED -> {
                val position = type.position
                val bukkitEntity = caster.bukkitEntity ?: return TickResult.INTERRUPT
                bukkitEntity.teleport(position.toLocation(bukkitEntity.world))
            }

            is Type.RANDOM -> {
                caster.bukkitEntity?.sendPlainMessage("随机传送, 最小距离: ${type.minDistance}, 最大距离: ${type.maxDistance}")
            }

            is Type.TARGET -> {
                val bukkitLocation = location.apply {
                    caster.bukkitEntity?.location?.pitch?.let { pitch = it }
                    caster.bukkitEntity?.location?.yaw?.let { yaw = it }
                }
                caster.bukkitEntity?.teleport(bukkitLocation)
            }
        }
        return TickResult.ALL_DONE
    }
}

internal object TeleportTypeSerializer : SchemaSerializer<Type> {
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
                Type.RANDOM(
                    node.node("min_distance").krequire<Int>(),
                    node.node("max_distance").krequire<Int>()
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown teleportation type: $teleportType")
            }
        }
    }
}
