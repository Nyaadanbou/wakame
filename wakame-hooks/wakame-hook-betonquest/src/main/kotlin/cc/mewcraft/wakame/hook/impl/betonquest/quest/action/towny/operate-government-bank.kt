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

enum class BankOperation {
    DEPOSIT,
    WITHDRAW,
}

class OperateGovernmentBank(
    private val logger: BetonQuestLogger,
    private val amount: Argument<Number>,
    private val operation: Argument<BankOperation>,
    private val govType: Argument<GovernmentType>,
) : PlayerAction {

    override fun execute(profile: Profile) {
        val playerId = profile.playerUUID
        when (govType.getValue(profile)) {
            GovernmentType.TOWN -> {
                val town = TownyLocal.getTown(playerId) ?: return
                when (operation.getValue(profile)) {
                    BankOperation.DEPOSIT -> town.deposit(amount.getValue(profile).toDouble())
                    BankOperation.WITHDRAW -> town.withdraw(amount.getValue(profile).toDouble())
                }
            }

            GovernmentType.NATION -> {
                val nation = TownyLocal.getNation(playerId) ?: return
                when (operation.getValue(profile)) {
                    BankOperation.DEPOSIT -> nation.deposit(amount.getValue(profile).toDouble())
                    BankOperation.WITHDRAW -> nation.withdraw(amount.getValue(profile).toDouble())
                }
            }
        }
    }
}

class OperateGovernmentBankFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val logger = loggerFactory.create(OperateGovernmentBank::class.java)
        val amount = instruction.number().atLeast(0.0).get()
        val operation = instruction.enumeration(BankOperation::class.java).get()
        val govType = instruction.enumeration(GovernmentType::class.java).get()
        return OperateGovernmentBank(logger, amount, operation, govType)
    }
}