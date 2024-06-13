package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import net.kyori.adventure.key.Key

interface CommandExecute : Skill {
    val commands: List<String>

    companion object Factory : SkillFactory<CommandExecute> {
        override fun create(key: Key, config: ConfigProvider): CommandExecute {
            val display = config.optionalEntry<SkillDisplay>("displays").orElse(EmptySkillDisplay)
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)
            val command = config.entry<List<String>>("commands")

            return Default(key, display, conditions, command)
        }
    }

    private class Default(
        override val key: Key,
        display: Provider<SkillDisplay>,
        conditions: Provider<SkillConditionGroup>,
        command: Provider<List<String>>
    ) : CommandExecute {
        override val displays: SkillDisplay by display
        override val conditions: SkillConditionGroup by conditions
        override val commands: List<String> by command

        override fun cast(context: SkillCastContext): SkillCastResult {
            val entity = context.optional(SkillCastContextKeys.CASTER_ENTITY)?.bukkitEntity ?: return FixedSkillCastResult.NONE_TARGET
            for (command in commands) {
                command.replace("{caster}", entity.name).also { entity.server.dispatchCommand(entity.server.consoleSender, it) }
            }
            return FixedSkillCastResult.SUCCESS
        }
    }
}