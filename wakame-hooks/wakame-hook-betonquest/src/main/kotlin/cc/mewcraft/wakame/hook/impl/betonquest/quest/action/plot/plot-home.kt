package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.plot

import com.plotsquared.bukkit.util.BukkitUtil
import com.plotsquared.core.PlotSquared
import com.plotsquared.core.events.TeleportCause
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import org.betonquest.betonquest.api.quest.action.online.OnlineActionAdapter
import kotlin.jvm.optionals.getOrNull


/**
 * 将玩家传送到他们位于 [dimension] 维度中的第 [order] 个地皮.
 *
 * @param order 要传送到的地皮顺序, 从 1 开始计数; 未指定则随机选择一个地皮
 * @param dimension 指定在哪个维度的地皮区域传送地皮, 未指定则使用第一个地皮区域
 * @param logger BetonQuest 的日志记录器
 */
class PlotHomeAction(
    private val order: Argument<Number>?,
    private val dimension: Argument<String>?,
    private val logger: BetonQuestLogger,
) : OnlineAction {

    companion object {
        const val RANDOM_ORDER: Int = -1
    }

    private val psApi: PlotSquared
        get() = PlotSquared.get()

    override fun execute(profile: OnlineProfile) {
        val orderValue = order?.getValue(profile)?.toInt() ?: RANDOM_ORDER
        val dimensionValue = dimension?.getValue(profile)
        val plotPlayer = BukkitUtil.adapt(profile.player)
        val plotArea = if (dimensionValue != null) {
            psApi.plotAreaManager.getPlotArea(dimensionValue, null) ?: run {
                logger.error("The dimension $dimensionValue has no plot area, aborting execution")
                return
            }
        } else {
            psApi.plotAreaManager.allPlotAreas.firstOrNull() ?: run {
                logger.error("No plot area found in this server, aborting execution")
                return
            }
        }
        val ownedPlots = plotArea.getPlots(profile.playerUUID).toList()
        val ownedPlotsCount = ownedPlots.size
        if (ownedPlotsCount <= 0) {
            logger.error("Player ${profile.player.name} has no plots in area ${plotArea.id}, cannot teleport")
            return
        }
        val targetPlot = if (orderValue == RANDOM_ORDER) {
            ownedPlots.random()
        } else {
            ownedPlots[orderValue.coerceIn(1, ownedPlotsCount) - 1]
        }
        targetPlot.teleportPlayer(plotPlayer, TeleportCause.PLUGIN) {}
    }
}


class PlotHomeActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val order = instruction.number().atLeast(1).get("order").getOrNull()
        val dimension = instruction.string().get("dimension").getOrNull()
        val logger = loggerFactory.create(PlotHomeAction::class.java)
        val questPackage = instruction.getPackage()
        val action = PlotHomeAction(order, dimension, logger)
        val adapter = OnlineActionAdapter(action, logger, questPackage)
        return adapter
    }
}