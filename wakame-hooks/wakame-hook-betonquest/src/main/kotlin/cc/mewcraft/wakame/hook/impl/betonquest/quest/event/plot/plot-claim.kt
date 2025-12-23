package cc.mewcraft.wakame.hook.impl.betonquest.quest.event.plot

import com.plotsquared.bukkit.util.BukkitUtil
import com.plotsquared.core.PlotSquared
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.event.PlayerEvent
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory
import org.betonquest.betonquest.api.quest.event.online.OnlineEvent
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter


/**
 * 为玩家领取一个地皮.
 *
 * @param skipIfExists 如果玩家已经拥有地皮, 是否跳过领取
 * @param dimension 指定在哪个维度的地皮区域领取地皮, 未指定则使用第一个地皮区域
 * @param logger BetonQuest 的日志记录器
 */
class PlotClaimEvent(
    private val skipIfExists: Boolean,
    private val dimension: Variable<String>?,
    private val logger: BetonQuestLogger,
) : OnlineEvent {

    private val psApi: PlotSquared
        get() = PlotSquared.get()

    override fun execute(profile: OnlineProfile) {
        val dimensionValue = dimension?.getValue(profile)
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
        val plotPlayer = BukkitUtil.adapt(profile.player)
        val plotCount = plotArea.getPlotCount(profile.playerUUID)
        if (plotCount > 0 && skipIfExists) {
            logger.info("Player ${profile.player.name} already has $plotCount plots in area ${plotArea.id}, skipping claim as per configuration")
            return
        }
        val nextFreePlot = plotArea.getNextFreePlot(plotPlayer, null)
        if (nextFreePlot != null) {
            val success = nextFreePlot.claim(plotPlayer, false, null, true, false)
            if (!success) {
                logger.error("Failed to claim a free plot for player ${profile.player.name}")
            } else {
                logger.info("Successfully claimed a plot for player ${profile.player.name} in area ${plotArea.id}")
            }
        }
    }
}


class PlotClaimEventFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerEventFactory {

    override fun parsePlayer(instruction: Instruction): PlayerEvent {
        val skipIfExists = instruction.hasArgument("skipIfExists")
        val dimension = instruction.getValue("dimension", instruction.parsers.string())
        val logger = loggerFactory.create(PlotClaimEvent::class.java)
        val questPackage = instruction.getPackage()
        val plotClaimEvent = PlotClaimEvent(skipIfExists, dimension, logger)
        return OnlineEventAdapter(plotClaimEvent, logger, questPackage)
    }
}