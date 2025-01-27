package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.WindowManager

@Init(
    stage = InitStage.POST_WORLD,
)
internal object GuiLifecycle {

    @InitFun
    fun init() {
        // 初始化 InvUI 的 static blocks
        // 随着 InvUI 更新, 这些代码可能会失效
        InvUI.getInstance()
        WindowManager.getInstance()
    }

    @DisableFun
    fun close() {
        closeEachWindow()
    }

    private fun closeEachWindow() {
        WindowManager.getInstance().windows.forEach(Window::close)
    }

}