package cc.mewcraft.wakame.skill.type

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup

interface CommandExecute : ConfiguredSkill {
    val commands: List<String>

    companion object Factory : SkillFactory<CommandExecute> {
        override fun create(config: ConfigProvider): CommandExecute {
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)
            val command = config.entry<List<String>>("commands")

            return Default(conditions, command)
        }
    }

    private class Default(
        conditions: Provider<SkillConditionGroup>,
        command: Provider<List<String>>
    ) : CommandExecute {
        override val conditions: SkillConditionGroup by conditions
        override val commands: List<String> by command
        override fun cast(context: SkillCastContext) {
            val target = context.target as Target.LivingEntity
            for (command in commands) {
                val entity = target.bukkitEntity
                command.replace("{target}", entity.name).also { entity.server.dispatchCommand(entity.server.consoleSender, it) }
            }
        }
    }
}