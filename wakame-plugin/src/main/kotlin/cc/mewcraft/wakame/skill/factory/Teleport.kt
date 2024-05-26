@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillCastContext
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
            val display = config.optionalEntry<SkillDisplay>("").orElse(EmptySkillDisplay)
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
        override val display: SkillDisplay by display
        override val conditions: SkillConditionGroup by conditions
        override val type: Teleportation by type

        override fun cast(context: SkillCastContext) {
            val caster = context.caster as? Caster.Player ?: throw NoTargetException()
            when (val type = type) {
                is Teleportation.FIXED -> {
                    val player = caster.bukkitPlayer
                    val position = type.position
                    player.teleport(position.toLocation(player.world))
                }

                is Teleportation.TARGET -> {
                    val target = context.target as? Target.Location ?: throw NoTargetException()
                    val player = caster.bukkitPlayer
                    val bukkitLocation = target.bukkitLocation.apply {
                        pitch = player.location.pitch
                        yaw = player.location.yaw
                    }
                    player.teleport(bukkitLocation)
                }
            }
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