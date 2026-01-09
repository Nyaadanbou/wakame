package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.koish

import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import org.betonquest.betonquest.api.quest.action.online.OnlineAction
import org.betonquest.betonquest.api.quest.action.online.OnlineActionAdapter

/**
 * 锁定玩家的冻结刻数 (ticks) 使其不受原版机制影响.
 */
class LockFreezeTicksAction(
    private val type: Argument<Type>,
) : OnlineAction {

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
 * [LockFreezeTicksAction] 的工厂类.
 */
class LockFreezeTicksActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val type = instruction.enumeration(LockFreezeTicksAction.Type::class.java).get()
        val logger = loggerFactory.create(LockFreezeTicksAction::class.java)
        val action = LockFreezeTicksAction(type)
        val questPackage = instruction.getPackage()
        val adapter = OnlineActionAdapter(action, logger, questPackage)
        return adapter
    }
}