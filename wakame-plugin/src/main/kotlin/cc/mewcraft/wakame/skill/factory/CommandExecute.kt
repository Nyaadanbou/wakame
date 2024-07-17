package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.*
import cc.mewcraft.wakame.tick.TickResult
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

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<CommandExecute> {
            return CommandExecuteTick(context, this, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }
    }
}

private class CommandExecuteTick(
    context: SkillContext,
    skill: CommandExecute,
    override val interruptTriggers: Provider<TriggerConditions>,
    override val forbiddenTriggers: Provider<TriggerConditions>
) : AbstractPlayerSkillTick<CommandExecute>(skill, context) {

    override fun tickCastPoint(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("命令执行前摇")
        return TickResult.ALL_DONE
    }

    override fun tickBackswing(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("命令执行后摇")
        return TickResult.ALL_DONE
    }

    override fun tickCast(tickCount: Long): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val entity = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitEntity ?: return TickResult.INTERRUPT
        for (command in skill.commands) {
            command.replace("{caster}", entity.name).also { entity.server.dispatchCommand(entity.server.consoleSender, it) }
        }
        return TickResult.ALL_DONE
    }
}