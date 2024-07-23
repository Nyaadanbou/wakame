package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

interface CommandExecute : Skill {

    /**
     * 执行的指令.
     */
    val commands: List<String>

    companion object Factory : SkillFactory<CommandExecute> {
        override fun create(key: Key, config: ConfigurationNode): CommandExecute {
            val commands = config.node("commands").krequire<List<String>>()
            return Impl(key, config, commands)
        }
    }

    private class Impl(
        override val key: Key,
        config: ConfigurationNode,
        override val commands: List<String>
    ) : CommandExecute, SkillBase(key, config) {

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<CommandExecute> {
            return CommandExecuteTick(context, this, triggerConditionGetter.interrupt, triggerConditionGetter.forbidden)
        }
    }
}

private class CommandExecuteTick(
    context: SkillContext,
    skill: CommandExecute,
    override val interruptTriggers: TriggerConditions,
    override val forbiddenTriggers: TriggerConditions,
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