package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
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

        override fun cast(entity: LivingEntity): SkillResult<CommandExecute> {
            return CommandExecuteResult(this, entity)
        }
    }
}

private class CommandExecuteResult(
    override val skill: CommandExecute,
    private val entity: LivingEntity
) : SkillResult<CommandExecute> {

    override fun executeCast() {
        for (command in skill.commands) {
            command.replace("{caster}", entity.name).also { entity.server.dispatchCommand(entity.server.consoleSender, it) }
        }
    }

    override fun executeCastPoint() {
        entity.sendPlainMessage("命令执行前摇")
    }

    override fun executeBackswing() {
        entity.sendPlainMessage("命令执行后摇")
    }
}