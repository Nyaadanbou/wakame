package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.EnterDungeonEventFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemSerializer
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest"])
object BetonQuestHook {

    init {
        val plugin = BetonQuest.getInstance()
        val logger = plugin.loggerFactory
        val data = plugin.primaryServerThreadData

        /* Quest Type Registries */

        // Condition

        // Event
        val eventRegistry = plugin.questRegistries.event()
        eventRegistry.register("playdungeon", EnterDungeonEventFactory(logger, data))

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
