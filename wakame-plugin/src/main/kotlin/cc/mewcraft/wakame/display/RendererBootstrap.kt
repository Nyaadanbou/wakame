package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.display2.RendererSystems
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.initializer.Initializable

/**
 * 用于初始化渲染器模块.
 */
internal object RendererBootstrap : Initializable {
    private fun loadLayout0() {
        for ((_, system) in RendererSystems.entries()) {
            system.config.loadLayout()
        }
    }

    private suspend fun postEvents0() {
        for ((_, system) in RendererSystems.entries()) {
            PluginEventBus.get().post(RendererConfigReloadEvent(system.config.rawTooltipKeys))
        }
    }

    override fun onPostWorld() = loadLayout0()
    override suspend fun onPostWorldAsync() = postEvents0()
    override fun onReload() = loadLayout0()
    override suspend fun onReloadAsync() = postEvents0()
}