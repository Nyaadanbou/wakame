package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.plot.HasPlotFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.plot.PlotClaimEventFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.plot.PlotHomeEventFactory
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "PlotSquared"], requireAll = true)
object PlotSquaredCompat {

    init {
        val plugin = BetonQuest.getInstance()
        val questTypeApi = plugin.questTypeApi
        val profileProvider = plugin.profileProvider
        val loggerFactory = plugin.loggerFactory
        val data = plugin.primaryServerThreadData

        /* Quest Type Registries */

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
        conditionRegistry.register("p2has", HasPlotFactory(loggerFactory))

        // Event
        val eventRegistry = plugin.questRegistries.event()
        eventRegistry.register("p2claim", PlotClaimEventFactory(loggerFactory))
        eventRegistry.register("p2home", PlotHomeEventFactory(loggerFactory))

        // Objective
        val objectiveRegistry = plugin.questRegistries.objective()

        // Variable
        val variableRegistry = plugin.questRegistries.variable()

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
        val scheduleRegistry = plugin.featureRegistries.eventScheduling()
    }
}