package cc.mewcraft.wakame.hook.impl.mythicdungeons

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.dungeon.DungeonBridge


@Hook(plugins = ["MythicDungeons"])
object MythicDungeonsHook {

    init {
        DungeonBridge.setImplementation(MythicDungeonBridge())
    }
}