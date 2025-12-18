package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.koish

import cc.mewcraft.wakame.hook.impl.betonquest.util.ArithmeticOp
import cc.mewcraft.wakame.hook.impl.betonquest.util.FriendlyEnumParser
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter
import org.betonquest.betonquest.api.quest.event.thread.PrimaryServerThreadEvent

/**
 * 修改玩家的 `TicksFrozen` 的数据值.
 */
class SetFreezeTicks(
    private val operation: Variable<ArithmeticOp>,
    private val amount: Variable<Number>,
    private val logger: BetonQuestLogger,
) : OnlineEvent {

    override fun execute(profile: OnlineProfile) {
        val player = profile.player
        val current = player.freezeTicks

        val op = operation.getValue(profile)
        val amountNumber = amount.getValue(profile)
        val amountInt = amountNumber.toInt()

        val result = when (op) {
            ArithmeticOp.ADD -> current + amountInt
            ArithmeticOp.SUBTRACT -> current - amountInt
            ArithmeticOp.MULTIPLY -> current * amountInt
            ArithmeticOp.DIVIDE -> {
                if (amountInt == 0) {
                    logger.warn("Attempted to divide freezeTicks by zero for player ${player.name}; operation ignored")
                    return
                }
                current / amountInt
            }

            ArithmeticOp.MODULO -> {
                if (amountInt == 0) {
                    logger.warn("Attempted to modulo freezeTicks by zero for player ${player.name}; operation ignored")
                    return
                }
                current % amountInt
            }
        }

        player.freezeTicks = result.coerceAtLeast(0)
    }
}

class SetFreezeTicksFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val data: PrimaryServerThreadData,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val operation = instruction.get(FriendlyEnumParser<ArithmeticOp>())
        val amount = instruction.get(Argument.NUMBER)
        val logger = loggerFactory.create(SetFreezeTicks::class.java)
        val event = SetFreezeTicks(operation, amount, logger)
        val questPackage = instruction.getPackage()
        val eventAdapter = OnlineEventAdapter(event, logger, questPackage)
        return PrimaryServerThreadEvent(eventAdapter, data)
    }
}