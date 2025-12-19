package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.plot

import com.plotsquared.core.PlotSquared
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter


/**
 * 检查玩家是否拥有指定数量的地皮.
 *
 * @param amount 需要拥有的地皮数量, 为空则默认为 1
 * @param dimension 地皮所属的维度, 未指定则检查第一个维度
 * @param logger BetonQuest 的日志记录器
 */
class HasPlot(
    private val amount: Variable<Number>?,
    private val dimension: Variable<String>?,
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    private val psApi: PlotSquared
        get() = PlotSquared.get()

    override fun check(profile: OnlineProfile): Boolean {
        val amountValue = amount?.getValue(profile)?.toInt() ?: 1
        val dimensionValue = dimension?.getValue(profile)
        val plotArea = if (dimensionValue != null) {
            psApi.plotAreaManager.getPlotArea(dimensionValue, null) ?: run {
                logger.error("The dimension $dimensionValue has no plot area, aborting condition check")
                return false
            }
        } else {
            psApi.plotAreaManager.allPlotAreas.firstOrNull() ?: run {
                logger.error("No plot area found in this server, aborting condition check")
                return false
            }
        }
        val ownedPlots = plotArea.getPlots(profile.playerUUID)
        val ownedPlotCount = ownedPlots.count()
        return ownedPlotCount >= amountValue
    }
}


class HasPlotFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val amount = instruction.getValue("amount", Argument.NUMBER_NOT_LESS_THAN_ONE)
        val dimension = instruction.getValue("dimension", Argument.STRING)
        val logger = loggerFactory.create(HasPlot::class.java)
        val hasPlot = HasPlot(amount, dimension, logger)
        val questPackage = instruction.getPackage()
        return OnlineConditionAdapter(hasPlot, logger, questPackage)
    }
}