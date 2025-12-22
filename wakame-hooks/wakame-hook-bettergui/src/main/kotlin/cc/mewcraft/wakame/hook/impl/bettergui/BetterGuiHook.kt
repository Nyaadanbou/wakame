package cc.mewcraft.wakame.hook.impl.bettergui

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item.behavior.impl.external.OpenExternalMenu

@Hook(plugins = ["BetterGUI"])
object BetterGuiHook {

    init {
        // 替换 OpenExternalMenu 物品行为的实现
        OpenExternalMenu.setImplementation(BetterGuiOpenExternalMenu)
    }
}
