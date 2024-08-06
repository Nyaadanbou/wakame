package cc.mewcraft.wakame.gui.rerolling

import cc.mewcraft.wakame.display.ItemRenderer
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.setSystemUse

class RerollingOutputItemRenderer : ItemRenderer<NekoStack> {
    override fun render(nekoStack: NekoStack) {
        // 关闭发包渲染
        nekoStack.setSystemUse()
        // 
    }
}
