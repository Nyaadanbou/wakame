package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 用于初始化渲染器模块.
 */
internal object RendererBootstrap : Initializable, KoinComponent {
    private val config: RendererConfig by inject()

    private fun loadLayout0() {
        config.loadLayout()
    }

    private suspend fun postEvents0() {
        PluginEventBus.get().post(RendererConfigReloadEvent(config.rawTooltipKeys))
    }

    override fun onPostWorld() = loadLayout0()
    override suspend fun onPostWorldAsync() = postEvents0()
    override fun onReload() = loadLayout0()
    override suspend fun onReloadAsync() = postEvents0()
}