package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.reforge.merge.SimpleMergingSession

internal object MergingSessionFactory {
    fun create(
        menu: MergingMenu,
        inputItem1: NekoStack?,
        inputItem2: NekoStack?,
    ): MergingSession {
        return SimpleMergingSession(menu.viewer, menu.table, inputItem1, inputItem2)
    }
}