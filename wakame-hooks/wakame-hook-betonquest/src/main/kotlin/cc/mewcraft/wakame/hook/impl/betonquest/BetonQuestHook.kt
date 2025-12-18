package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.party.HasPartyFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party.CreatePartyEventFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party.LeavePartyEventFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemSerializer
import cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule.GameTickScheduleFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule.GameTickScheduler
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest
import org.betonquest.betonquest.schedule.EventScheduling

@Hook(plugins = ["BetonQuest"])
object BetonQuestHook {

    init {
        val plugin = BetonQuest.getInstance()
        val questTypeApi = plugin.questTypeApi
        val profileProvider = plugin.profileProvider
        val loggerFactory = plugin.loggerFactory
        val variableProcessor = plugin.variableProcessor
        val packManager = plugin.questPackageManager
        val data = plugin.primaryServerThreadData

        /* Quest Type Registries */

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
        conditionRegistry.register("hasparty", HasPartyFactory(loggerFactory))

        // Event
        val eventRegistry = plugin.questRegistries.event()
        eventRegistry.register("createparty", CreatePartyEventFactory(loggerFactory, questTypeApi, profileProvider))
        eventRegistry.register("leaveparty", LeavePartyEventFactory(loggerFactory))

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
        itemRegistry.register("koish", KoishQuestItemFactory())
        itemRegistry.registerSerializer("koish", KoishQuestItemSerializer())

        // TextParser
        val textParserRegistry = plugin.featureRegistries.textParser()

        // NotifyIO
        val notifyIoRegistry = plugin.featureRegistries.notifyIO()

        // Schedule
        val scheduleRegistry = plugin.featureRegistries.eventScheduling()
        scheduleRegistry.register(
            "game-tick",
            EventScheduling.ScheduleType(
                GameTickScheduleFactory(variableProcessor, packManager),
                GameTickScheduler(loggerFactory.create(GameTickScheduler::class.java), questTypeApi)
            ),
        )
    }
}
