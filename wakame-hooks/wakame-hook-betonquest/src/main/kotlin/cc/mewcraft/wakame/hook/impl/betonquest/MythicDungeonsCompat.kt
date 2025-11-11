package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.dungeon.MythicDungeonsListener
import cc.mewcraft.wakame.hook.impl.betonquest.quest.event.EnterDungeonEventFactory
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.registerEvents
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "MythicDungeons"])
object MythicDungeonsCompat {

    init {
        val loggerFactory = BetonQuest.getInstance().loggerFactory
        val primaryServerThreadData = BetonQuest.getInstance().primaryServerThreadData
        val questRegistries = BetonQuest.getInstance().questRegistries

        questRegistries.event().register("playdungeon", EnterDungeonEventFactory(loggerFactory, primaryServerThreadData))
    }

    init {
        MythicDungeonsListener.registerEvents()
    }
}