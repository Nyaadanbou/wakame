package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.reforge.mod.SimpleModdingSession

internal object ModdingSessionFactory {
    /**
     * 创建一个 [ModdingSession].
     *
     * @param menu 定制台的菜单
     * @param input 要定制的物品, 如果没有则传入 null
     */
    fun create(
        menu: ModdingMenu,
        input: NekoStack?,
    ): ModdingSession {
        return SimpleModdingSession(menu.table, menu.viewer, input)
    }
}