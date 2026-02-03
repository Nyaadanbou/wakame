package cc.mewcraft.wakame.hook.impl.hibiscuscommons

import cc.mewcraft.wakame.integration.Hook
import me.lojosho.hibiscuscommons.hooks.Hooks

@Hook(plugins = ["HibiscusCommons"])
object HibiscusCommonsHook {

    init {
        Hooks.addHook(KoishHook())
    }
}