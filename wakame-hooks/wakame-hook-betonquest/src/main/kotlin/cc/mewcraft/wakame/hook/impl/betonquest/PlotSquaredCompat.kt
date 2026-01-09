package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.plot.PlotClaimActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.plot.PlotHomeActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.plot.HasPlotFactory
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "PlotSquared"], requireAll = true)
object PlotSquaredCompat {

    init {
        val plugin = BetonQuest.getInstance()
        val questTypeApi = plugin.questTypeApi
        val profileProvider = plugin.profileProvider
        val loggerFactory = plugin.loggerFactory

        /* Quest Type Registries */

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
        conditionRegistry.register("p2has", HasPlotFactory(loggerFactory))

        // Action
        val actionRegistry = plugin.questRegistries.action()
        actionRegistry.register("p2claim", PlotClaimActionFactory(loggerFactory))
        actionRegistry.register("p2home", PlotHomeActionFactory(loggerFactory))

        // Objective
        val objectiveRegistry = plugin.questRegistries.objective()

        // Variable
        val placeholderRegistry = plugin.questRegistries.placeholder()

        /* Feature Registries */

        // ConversationIO
        val conversationIORegistry = plugin.featureRegistries.conversationIO()

        // Interceptor
        val interceptorRegistry = plugin.featureRegistries.interceptor()

        // Item
        val itemRegistry = plugin.featureRegistries.item()

        // TextParser
        val textParserRegistry = plugin.featureRegistries.textParser()

        // NotifyIO
        val notifyIORegistry = plugin.featureRegistries.notifyIO()

        // Schedule
        val scheduleRegistry = plugin.featureRegistries.actionScheduling()
    }
}