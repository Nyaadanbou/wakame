package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.RendererFormats
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

internal abstract class AbstractRendererFormats : RendererFormats, KoinComponent {
    protected val logger by inject<Logger>()

    /**
     * 设置指定的 [RendererFormat].
     */
    abstract fun <T : RendererFormat> set(id: String, format: T)
}
