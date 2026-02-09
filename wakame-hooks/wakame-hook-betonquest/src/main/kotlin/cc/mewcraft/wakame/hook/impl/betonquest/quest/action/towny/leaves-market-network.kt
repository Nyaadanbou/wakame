package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.towny

import cc.mewcraft.wakame.integration.towny.GovernmentType
import cc.mewcraft.wakame.integration.towny.TownyLocal
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory

class LeavesMarketNetwork(
    private val logger: BetonQuestLogger,
    private val govType: Argument<GovernmentType>,
) : PlayerAction {

    override fun execute(profile: Profile) {
        val playerId = profile.playerUUID
        when (govType.getValue(profile)) {
            GovernmentType.TOWN -> {
                val town = TownyLocal.getTown(playerId) ?: return
                TownyLocal.leavesMarketNetwork(town)
            }

            GovernmentType.NATION -> {
                val nation = TownyLocal.getNation(playerId) ?: return
                TownyLocal.leavesMarketNetwork(nation)
            }
        }
    }
}

class LeavesMarketNetworkFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val logger = loggerFactory.create(LeavesMarketNetwork::class.java)
        val govType = instruction.enumeration(GovernmentType::class.java).get()
        return LeavesMarketNetwork(logger, govType)
    }
}