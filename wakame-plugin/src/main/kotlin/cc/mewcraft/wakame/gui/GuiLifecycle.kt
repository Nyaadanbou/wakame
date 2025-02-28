package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.WindowManager

@Init(stage = InitStage.POST_WORLD)
internal object GuiLifecycle {

    @DisableFun
    fun close() {
        closeEachWindow()
    }

    private fun closeEachWindow() {
        WindowManager.getInstance().windows.forEach(Window::close)
    }

}