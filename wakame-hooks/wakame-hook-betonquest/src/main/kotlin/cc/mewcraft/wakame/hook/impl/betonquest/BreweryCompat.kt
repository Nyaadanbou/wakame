// 用 Brewery 代替 TheBrewingProject 这个冗长的名字

package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.BrewQuestItemFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.BrewQuestItemSerializer
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "TheBrewingProject"])
object BreweryCompat {

    init {
        val plugin = BetonQuest.getInstance()
        val questTypeApi = plugin.questTypeApi
        val profileProvider = plugin.profileProvider
        val loggerFactory = plugin.loggerFactory
        val data = plugin.primaryServerThreadData

        /* Quest Type Registries */

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()

        // Event
        val eventRegistry = plugin.questRegistries.event()

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
        itemRegistry.register("brew", BrewQuestItemFactory())
        itemRegistry.registerSerializer("brew", BrewQuestItemSerializer())

        // TextParser
        val textParserRegistry = plugin.featureRegistries.textParser()

        // NotifyIO
        val notifyIORegistry = plugin.featureRegistries.notifyIO()

        // Schedule
        val scheduleRegistry = plugin.featureRegistries.eventScheduling()
    }
}