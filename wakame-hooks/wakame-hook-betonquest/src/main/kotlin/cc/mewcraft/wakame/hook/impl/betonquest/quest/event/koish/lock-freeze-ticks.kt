package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.koish

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter
import org.betonquest.betonquest.api.quest.event.thread.PrimaryServerThreadEvent

/**
 * 锁定玩家的冻结刻数 (ticks) 使其不受原版机制影响.
 */
class LockFreezeTicks(
    private val type: Variable<Type>,
) : OnlineEvent {

    override fun execute(profile: OnlineProfile) {
        val player = profile.player
        val action = type.getValue(profile)

        when (action) {
            Type.LOCK -> player.lockFreezeTicks(true)
            Type.UNLOCK -> player.lockFreezeTicks(false)
        }
    }

    enum class Type {
        /**
         * 相当于给 [org.bukkit.entity.Player.lockFreezeTicks] 传入 `true` 参数.
         */
        LOCK,

        /**
         * 相当于给 [org.bukkit.entity.Player.lockFreezeTicks] 传入 `false` 参数.
         */
        UNLOCK,
    }
}

/**
 * [LockFreezeTicks] 的工厂类.
 */
class LockFreezeTicksFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val data: PrimaryServerThreadData,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val type = instruction.get(Argument.ENUM(LockFreezeTicks.Type::class.java))
        val logger = loggerFactory.create(LockFreezeTicks::class.java)
        val event = LockFreezeTicks(type)
        val questPackage = instruction.getPackage()
        val eventAdapter = OnlineEventAdapter(event, logger, questPackage)
        return PrimaryServerThreadEvent(eventAdapter, data)
    }
}