package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.KoishPlugin
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.WindowManager

@Init(stage = InitStage.POST_WORLD)
@Reload
internal object InvUiLifecycle {

    @InitFun
    fun init() {
        // See: https://docs.xen.cx/invui/#paper-plugin
        InvUI.getInstance().setPlugin(KoishPlugin)

        // 趁早初始化 WindowManager 的实例
        WindowManager.getInstance()
    }

    // 关服时/重载时关闭所有 Window 以避免一些未知的问题
    @ReloadFun
    @DisableFun
    fun close() {
        closeEachWindow()
    }

    private fun closeEachWindow() {
        WindowManager.getInstance().windows.forEach(Window::close)
    }

}