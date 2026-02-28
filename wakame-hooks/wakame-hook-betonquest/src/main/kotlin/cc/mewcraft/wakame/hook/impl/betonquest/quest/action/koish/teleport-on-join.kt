package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.koish

import cc.mewcraft.wakame.feature.TeleportOnJoin
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.OnlineActionAdapter
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import org.bukkit.event.player.PlayerTeleportEvent


/**
 * 将玩家传送到 [cc.mewcraft.wakame.feature.TeleportOnJoinConfig.target].
 */
class TeleportOnJoinAction(
    private val logger: BetonQuestLogger,
) : OnlineAction {

    override fun execute(profile: OnlineProfile) {
        val player = profile.player
        val target = TeleportOnJoin.config.target
        player.teleportAsync(target, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }
}

class TeleportOnJoinActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction? {
        val logger = loggerFactory.create(TeleportOnJoinAction::class.java)
        val action = TeleportOnJoinAction(logger)
        val adapter = OnlineActionAdapter(action)
        return adapter
    }
}