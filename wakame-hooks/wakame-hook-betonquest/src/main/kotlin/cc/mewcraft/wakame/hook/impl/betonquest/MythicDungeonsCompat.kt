package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.listener.MythicDungeonsListener
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon.AwaitingDungeonFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon.InsideDungeonFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.dungeon.EnterDungeonEventFactory
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.registerEvents
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "MythicDungeons"], requireAll = true)
object MythicDungeonsCompat {

    init {
        val plugin = BetonQuest.getInstance()
        val loggerFactory = BetonQuest.getInstance().loggerFactory
        val questRegistries = BetonQuest.getInstance().questRegistries

        /* Quest Type Registries */

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
        conditionRegistry.register("awaitingdungeon", AwaitingDungeonFactory(loggerFactory))
        conditionRegistry.register("insidedungeon", InsideDungeonFactory(loggerFactory))

        // Event
        val eventRegistry = plugin.questRegistries.event()
        questRegistries.event().register("playdungeon", EnterDungeonEventFactory(loggerFactory))

        // Objective
        val objectiveRegistry = plugin.questRegistries.objective()

        // Variable
        val variableRegistry = plugin.questRegistries.variable()

        /* Feature Registries */

        // ConversationIO
        val conversationIoRegistry = plugin.featureRegistries.conversationIO()

        // Interceptor
        val interceptorRegistry = plugin.featureRegistries.interceptor()

        // Item
        val itemRegistry = plugin.featureRegistries.item()

        // TextParser
        val textParserRegistry = plugin.featureRegistries.textParser()

        // NotifyIO
        val notifyIoRegistry = plugin.featureRegistries.notifyIO()

        // Schedule
        val scheduleRegistry = plugin.featureRegistries.eventScheduling()
    }

    init {
        MythicDungeonsListener.registerEvents()
    }
}