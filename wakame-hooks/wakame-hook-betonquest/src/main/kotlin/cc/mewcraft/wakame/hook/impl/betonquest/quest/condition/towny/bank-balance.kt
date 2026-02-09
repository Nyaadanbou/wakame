package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny

import cc.mewcraft.wakame.hook.impl.betonquest.util.ComparisonOp
import cc.mewcraft.wakame.integration.towny.GovernmentType
import cc.mewcraft.wakame.integration.towny.TownyLocal
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory

class GovernmentBankBalance(
    private val logger: BetonQuestLogger,
    private val amount: Argument<Number>,
    private val comparison: Argument<ComparisonOp>,
    private val govType: Argument<GovernmentType>,
) : PlayerCondition {

    override fun check(profile: Profile): Boolean {
        val playerId = profile.playerUUID

        val balance = when (govType.getValue(profile)) {
            GovernmentType.TOWN -> {
                val town = TownyLocal.getTown(playerId) ?: return false
                town.balance
            }

            GovernmentType.NATION -> {
                val nation = TownyLocal.getNation(playerId) ?: return false
                nation.balance
            }
        }

        val comparison = comparison.getValue(profile)
        val target = amount.getValue(profile).toDouble()

        return when (comparison) {
            ComparisonOp.LESS_THAN -> balance < target
            ComparisonOp.LESS_THAN_OR_EQUAL -> balance <= target
            ComparisonOp.EQUAL -> balance == target
            ComparisonOp.GREATER_THAN_OR_EQUAL -> balance >= target
            ComparisonOp.GREATER_THAN -> balance > target
        }
    }
}

class GovernmentBankBalanceFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val logger = loggerFactory.create(GovernmentBankBalance::class.java)
        val amount = instruction.number().atLeast(0).get()
        val operation = instruction.enumeration(ComparisonOp::class.java).get()
        val govType = instruction.enumeration(GovernmentType::class.java).get()
        return GovernmentBankBalance(logger, amount, operation, govType)
    }
}