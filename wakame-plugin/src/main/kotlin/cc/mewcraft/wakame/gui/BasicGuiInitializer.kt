package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.KoishPlugin
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMenuStacks
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.WindowManager

@Init(stage = InitStage.POST_WORLD)
@Reload
internal object BasicGuiInitializer : Listener {

    @InitFun
    private fun init() {
        // See: https://docs.xen.cx/invui/#paper-plugin
        InvUI.getInstance().setPlugin(KoishPlugin)

        // 趁早初始化 WindowManager 的实例
        WindowManager.getInstance()

        // 注册事件监听器
        registerEvents()
    }

    @DisableFun
    @ReloadFun
    private fun close() {
        // 关服时/重载时关闭所有 Window 以避免一些未知的问题
        WindowManager.getInstance().windows.forEach(Window::close)
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        // 玩家退出服务器后移除其图鉴菜单栈
        CatalogItemMenuStacks.removeStack(event.player)
    }
}