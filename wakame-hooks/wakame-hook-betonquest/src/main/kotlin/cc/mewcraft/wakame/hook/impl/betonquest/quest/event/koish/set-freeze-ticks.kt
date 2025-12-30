package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.koish

import cc.mewcraft.wakame.hook.impl.betonquest.util.ArithmeticOp
import cc.mewcraft.wakame.hook.impl.betonquest.util.FriendlyEnumParser
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter

/**
 * 修改玩家的 `TicksFrozen` 的数据值.
 */
class SetFreezeTicks(
    private val operation: Argument<ArithmeticOp>,
    private val amount: Argument<Number>,
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

        // 根据 Minecraft Wiki: freezeTicks 增加到 140 ticks 时就会冻结玩家.
        // Player#setFreezeTicks 不会限制 freezeTicks 的最大值, 需要手动限制.
        player.freezeTicks = result.coerceIn(0, player.maxFreezeTicks)
    }
}

class SetFreezeTicksFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val operation = instruction.parse(FriendlyEnumParser<ArithmeticOp>()).get()
        val amount = instruction.number().get()
        val logger = loggerFactory.create(SetFreezeTicks::class.java)
        val event = SetFreezeTicks(operation, amount, logger)
        val questPackage = instruction.getPackage()
        val adapter = OnlineEventAdapter(event, logger, questPackage)
        return adapter
    }
}