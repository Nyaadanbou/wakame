package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon.AwaitingDungeonFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon.InsideDungeonFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.party.HasPartyFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.dungeon.EnterDungeonEventFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party.CreatePartyEventFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.party.LeavePartyEventFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemSerializer
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest"])
object BetonQuestHook {

    init {
        val plugin = BetonQuest.getInstance()
        val questTypeApi = plugin.questTypeApi
        val profileProvider = plugin.profileProvider
        val loggerFactory = plugin.loggerFactory
        val data = plugin.primaryServerThreadData

        /* Quest Type Registries */

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
        conditionRegistry.register("insidedungeon", InsideDungeonFactory(loggerFactory, data))
        conditionRegistry.register("hasparty", HasPartyFactory(loggerFactory))

        // Event
        val eventRegistry = plugin.questRegistries.event()
        eventRegistry.register("playdungeon", EnterDungeonEventFactory(loggerFactory, data))
        eventRegistry.register("createparty", CreatePartyEventFactory(loggerFactory, questTypeApi, profileProvider))
        eventRegistry.register("leaveparty", LeavePartyEventFactory(loggerFactory))

        // Objective

        // Variable

        /* Feature Registries */

        // ConversationIO

        // Interceptor

        // Item
        val itemRegistry = plugin.featureRegistries.item()
        itemRegistry.register("koish", KoishQuestItemFactory())
        itemRegistry.registerSerializer("koish", KoishQuestItemSerializer())

        // TextParser

        // NotifyIO

        // Schedule
    }
}
