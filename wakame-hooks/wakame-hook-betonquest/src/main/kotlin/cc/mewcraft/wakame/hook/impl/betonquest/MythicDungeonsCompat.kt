package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.listener.MythicDungeonsListener
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.dungeon.EnterDungeonActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon.AwaitingDungeonFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.dungeon.InsideDungeonFactory
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.registerEvents

@Hook(plugins = ["BetonQuest", "MythicDungeons"], requireAll = true)
object MythicDungeonsCompat {

    init {
        hook {
            conditions {
                register("awaitingdungeon", AwaitingDungeonFactory(api.loggerFactory()))
                register("insidedungeon", InsideDungeonFactory(api.loggerFactory()))
            }
            actions {
                register("playdungeon", EnterDungeonActionFactory(api.loggerFactory()))
            }
        }
    }

    init {
        MythicDungeonsListener.registerEvents()
    }
}