package cc.mewcraft.wakame.hook.impl

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.util.registerEvents

@Hook(plugins = ["HuskHomes"])
object HuskHomesHook {

    init {
        TpaBlockListener().registerEvents()
    }
}
