package cc.mewcraft.wakame.gui

import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.WindowManager

internal object GuiManager {
    fun closeAll() {
        WindowManager.getInstance().windows.forEach(Window::close)
    }
}