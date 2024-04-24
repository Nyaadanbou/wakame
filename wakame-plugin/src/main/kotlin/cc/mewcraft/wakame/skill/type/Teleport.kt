@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.skill.type

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import io.papermc.paper.math.Position

interface Teleport : ConfiguredSkill {
    val type: TeleportType

    sealed interface TeleportType {
        data class FIXED(val position: Position) : TeleportType
        data object TARGET : TeleportType
    }

    companion object Factory : SkillFactory<Teleport> {
        override fun create(config: ConfigProvider): Teleport {
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)
            val type = config.optionalEntry<TeleportType>("type")
                .orElse(TeleportType.FIXED(Position.block(0, 0, 0)))

            return Default(conditions, type)
        }
    }

    private class Default(
        conditions: Provider<SkillConditionGroup>,
        type: Provider<TeleportType>
    ) : Teleport {
        override val conditions: SkillConditionGroup by conditions
        override val type: TeleportType by type

        override fun cast(context: SkillCastContext) {
            val caster = context.caster as Caster.Player
            when (val type = type) {
                is TeleportType.FIXED -> {
                    val player = caster.bukkitPlayer
                    val position = type.position
                    player.teleport(position.toLocation(player.world))
                }

                is TeleportType.TARGET -> {
                    val target = context.target as Target.Location
                    val player = caster.bukkitPlayer
                    player.teleport(target.bukkitLocation)
                }
            }
        }
    }
}