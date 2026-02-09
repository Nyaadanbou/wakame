package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny

import cc.mewcraft.wakame.integration.towny.TownyLocal
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory

enum class RankType {
    MAYOR, KING
}

class TownyRank(
    private val logger: BetonQuestLogger,
    private val rankType: Argument<RankType>,
) : PlayerCondition {

    override fun check(profile: Profile): Boolean {
        return when (rankType.getValue(profile)) {
            RankType.MAYOR -> TownyLocal.isMayor(profile.playerUUID)
            RankType.KING -> TownyLocal.isKing(profile.playerUUID)
        }
    }
}

class TownyRankFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val logger = loggerFactory.create(TownyRank::class.java)
        val rankType = instruction.enumeration(RankType::class.java).get()
        return TownyRank(logger, rankType)
    }
}