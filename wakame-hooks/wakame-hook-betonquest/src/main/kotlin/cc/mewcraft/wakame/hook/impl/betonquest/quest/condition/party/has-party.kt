package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.party

import cc.mewcraft.wakame.integration.party.PartyIntegration
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.OnlineConditionAdapter
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import kotlin.jvm.optionals.getOrNull


/**
 * 检查玩家是否属于任意小队.
 *
 * @param amount 小队成员的最小数量
 */
class HasParty(
    private val amount: Argument<Number>?,
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        val amount = amount?.getValue(profile)?.toInt() ?: -1
        val party = PartyIntegration.lookupPartyByPlayer(profile.playerUUID).join()
        return party != null && party.members.size >= amount
    }
}


class HasPartyFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val amount = instruction.number().atLeast(0).get("amount").getOrNull()
        val logger = loggerFactory.create(HasParty::class.java)
        val hasParty = HasParty(amount, logger)
        return OnlineConditionAdapter(hasParty)
    }
}