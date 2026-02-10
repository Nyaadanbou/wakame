package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny

import cc.mewcraft.wakame.integration.towny.GovernmentType
import cc.mewcraft.wakame.integration.towny.TownyLocal
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory

class HasJoinedMarketNetwork(
    private val logger: BetonQuestLogger,
    private val govType: Argument<GovernmentType>,
) : PlayerCondition {

    override fun check(profile: Profile): Boolean {
        val playerId = profile.playerUUID
        val government = when (govType.getValue(profile)) {
            GovernmentType.TOWN -> TownyLocal.getTown(playerId)
            GovernmentType.NATION -> TownyLocal.getNation(playerId)
        } ?: return false
        return government.hasJoinedMarketNetwork()
    }
}

class HasJoinedMarketNetworkFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val logger = loggerFactory.create(GovernmentBankBalance::class.java)
        val govType = instruction.enumeration(GovernmentType::class.java).get()
        return HasJoinedMarketNetwork(logger, govType)
    }
}