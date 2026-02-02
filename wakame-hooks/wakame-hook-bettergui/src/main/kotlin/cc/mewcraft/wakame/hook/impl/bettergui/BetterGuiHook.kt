package cc.mewcraft.wakame.hook.impl.bettergui

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.externalmenu.ExternalMenu

@Hook(plugins = ["BetterGUI"])
object BetterGuiHook {

    init {
        // 替换 OpenExternalMenu 物品行为的实现
        ExternalMenu.setImplementation(BetterGuiExternalMenu)
    }
}
