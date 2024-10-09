package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.RendererFormats
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger

internal abstract class AbstractRendererFormats : RendererFormats, KoinComponent {
    protected val formats = HashMap<String, RendererFormat>()
    protected val logger = get<Logger>()

    /**
     * 设置指定的 [RendererFormat].
     */
    abstract fun <T : RendererFormat> set(id: String, format: T)
}
