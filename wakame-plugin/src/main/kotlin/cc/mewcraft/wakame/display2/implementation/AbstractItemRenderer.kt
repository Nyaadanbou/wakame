/**
 * 关于各种渲染器的共同实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

internal abstract class AbstractItemRenderer<in T, in C> : ItemRenderer<T, C>, KoinComponent {
    protected val logger by inject<Logger>()

    /**
     * 渲染布局.
     */
    abstract val rendererLayout: RendererLayout

    /**
     * 渲染格式.
     */
    abstract val rendererFormats: RendererFormats
}

/**
 * 聚合了渲染一种物品数据所需要的数据和逻辑.
 *
 * @param T 被渲染的数据的类型
 * @param F 渲染格式的类型
 */
internal interface RenderingPart<T, F> {
    val format: F
    val renderer: IndexedDataRenderer<T, F>
}

internal interface RenderingPart2<T1, T2, F> {
    val format: F
    val renderer: IndexedDataRenderer2<T1, T2, F>
}

internal interface RenderingPart3<T1, T2, T3, F> {
    val format: F
    val renderer: IndexedDataRenderer3<T1, T2, T3, F>
}

internal interface RenderingPart4<T1, T2, T3, T4, F> {
    val format: F
    val renderer: IndexedDataRenderer4<T1, T2, T3, T4, F>
}

internal interface RenderingPart5<T1, T2, T3, T4, T5, F> {
    val format: F
    val renderer: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>
}

internal interface RenderingPart6<T1, T2, T3, T4, T5, T6, F> {
    val format: F
    val renderer: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>
}