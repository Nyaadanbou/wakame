package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.initializer.Initializable

/**
 * 用于初始化渲染器模块.
 */
internal object RendererBootstrap : Initializable {
    private fun loadLayout0() {
        for ((_, system) in ItemRenderers.entries()) {
            system.config.loadLayout()
        }
    }

    override fun onPostWorld() = loadLayout0()
    override fun onReload() = loadLayout0()
}