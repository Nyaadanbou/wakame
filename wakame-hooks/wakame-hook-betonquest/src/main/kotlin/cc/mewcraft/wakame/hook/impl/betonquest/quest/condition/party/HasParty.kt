package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.party

import cc.mewcraft.wakame.integration.party.PartyIntegration
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition

/**
 * 检查玩家是否属于任意小队.
 *
 * @param amount 小队成员的最小数量
 */
class HasParty(
    private val amount: Variable<Number>?,
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        val amount = amount?.getValue(profile)?.toInt() ?: -1
        val party = PartyIntegration.lookupPartyByPlayer(profile.playerUUID).join()
        return party != null && party.members.size >= amount
    }
}