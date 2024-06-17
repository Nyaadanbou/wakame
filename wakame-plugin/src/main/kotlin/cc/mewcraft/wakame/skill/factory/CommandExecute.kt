package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.skill.FixedSkillCastResult
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.SkillCastResult
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import net.kyori.adventure.key.Key

interface CommandExecute : Skill {

    /**
     * 执行的指令.
     */
    val commands: List<String>

    companion object Factory : SkillFactory<CommandExecute> {
        override fun create(key: Key, config: ConfigProvider): CommandExecute {
            val commands = config.entry<List<String>>("commands")
            return DefaultImpl(key, config, commands)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        commands: Provider<List<String>>,
    ) : CommandExecute, SkillBase(key, config) {

        override val commands: List<String> by commands

        override fun cast(context: SkillCastContext): SkillCastResult {
            val entity = context.optional(SkillCastContextKey.CASTER_ENTITY)?.bukkitEntity ?: return FixedSkillCastResult.NONE_CASTER
            for (command in commands) {
                command.replace("{caster}", entity.name).also { entity.server.dispatchCommand(entity.server.consoleSender, it) }
            }
            return FixedSkillCastResult.SUCCESS
        }
    }
}