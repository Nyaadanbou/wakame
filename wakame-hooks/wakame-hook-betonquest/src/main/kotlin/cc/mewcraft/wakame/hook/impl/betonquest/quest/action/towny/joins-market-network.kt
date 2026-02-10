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

class JoinsMarketNetwork(
    private val logger: BetonQuestLogger,
    private val govType: Argument<GovernmentType>,
) : PlayerAction {

    override fun execute(profile: Profile) {
        val playerId = profile.playerUUID
        val government = when (govType.getValue(profile)) {
            GovernmentType.TOWN -> TownyLocal.getTown(playerId)
            GovernmentType.NATION -> TownyLocal.getNation(playerId)
        } ?: return
        government.joinsMarketNetwork()
    }
}

class JoinsMarketNetworkFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val logger = loggerFactory.create(JoinsMarketNetwork::class.java)
        val govType = instruction.enumeration(GovernmentType::class.java).get()
        return JoinsMarketNetwork(logger, govType)
    }
}