package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.KoishPlugin
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMenuStacks
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.WindowManager

@Init(InitStage.POST_WORLD)
internal object BasicGuiInitializer : Listener {

    @InitFun
    fun init() {
        // See: https://docs.xen.cx/invui/#paper-plugin
        InvUI.getInstance().setPlugin(KoishPlugin)

        // 趁早初始化 WindowManager 的实例
        WindowManager.getInstance()

        // 注册事件监听器
        registerEvents()
    }

    @DisableFun
    fun close() {
        closeWindows()
    }

    fun reload() {
        closeWindows()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        // 玩家退出服务器后移除其图鉴菜单栈
        CatalogItemMenuStacks.removeStack(event.player)
    }

    private fun closeWindows() {
        WindowManager.getInstance().windows.forEach(Window::close)
    }
}