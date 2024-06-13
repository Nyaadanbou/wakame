@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.util.krequire
import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface Teleport : Skill {
    val type: Teleportation

    companion object Factory : SkillFactory<Teleport> {
        override fun create(key: Key, config: ConfigProvider): Teleport {
            val display = config.optionalEntry<SkillDisplay>("displays").orElse(EmptySkillDisplay)
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)
            val type = config.optionalEntry<Teleportation>("teleportation")
                .orElse(Teleportation.FIXED(Position.block(0, 0, 0)))

            return Default(key, display, conditions, type)
        }
    }

    private class Default(
        override val key: Key,
        display: Provider<SkillDisplay>,
        conditions: Provider<SkillConditionGroup>,
        type: Provider<Teleportation>
    ) : Teleport {
        override val displays: SkillDisplay by display
        override val conditions: SkillConditionGroup by conditions
        override val type: Teleportation by type

        override fun cast(context: SkillCastContext): SkillCastResult {
            val player = context.optional(SkillCastContextKeys.CASTER_PLAYER) ?: return FixedSkillCastResult.NONE_CASTER
            val location = context.optional(SkillCastContextKeys.TARGET_LOCATION)?.bukkitLocation ?: return FixedSkillCastResult.NONE_TARGET
            when (val type = type) {
                is Teleportation.FIXED -> {
                    val position = type.position
                    player.bukkitPlayer.teleport(position.toLocation(player.bukkitPlayer.world))
                }

                is Teleportation.TARGET -> {
                    val bukkitLocation = location.apply {
                        pitch = player.bukkitPlayer.location.pitch
                        yaw = player.bukkitPlayer.location.yaw
                    }
                    player.bukkitPlayer.teleport(bukkitLocation)
                }
            }
            return FixedSkillCastResult.SUCCESS
        }
    }
}

sealed interface Teleportation {
    data class FIXED(val position: Position) : Teleportation
    data object TARGET : Teleportation
}

internal object TeleportationSerializer : SchemaSerializer<Teleportation> {
    override fun deserialize(type: Type, node: ConfigurationNode): Teleportation {
        return when (val teleportationType = node.node("type").krequire<String>()) {
            "fixed" -> Teleportation.FIXED(
                Position.block(
                    node.node("x").krequire<Int>(),
                    node.node("y").krequire<Int>(),
                    node.node("z").krequire<Int>()
                )
            )

            "target" -> Teleportation.TARGET
            else -> throw IllegalArgumentException("Unknown teleportation type: $teleportationType")
        }
    }
}