/**
 * 关于各种渲染器的共同实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
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

/* RenderingPart */

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

/* RenderingPart Implementations */

internal class SimpleRenderingPart<T, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer<T, F>,
) : RenderingPart<T, F> {
    override val renderer = renderer
    override val format: F by format
    fun process(collector: MutableList<IndexedText>, data: T) {
        collector += renderer.render(data, format)
    }
}

internal class SimpleRenderingPart2<T1, T2, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer2<T1, T2, F>,
) : RenderingPart2<T1, T2, F> {
    override val format: F by format
    override val renderer = renderer
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2) {
        collector += renderer.render(data1, data2, format)
    }
}

internal class SimpleRenderingPart3<T1, T2, T3, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer3<T1, T2, T3, F>,
) : RenderingPart3<T1, T2, T3, F> {
    override val renderer = renderer
    override val format: F by format
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3) {
        collector += renderer.render(data1, data2, data3, format)
    }
}

internal class SimpleRenderingPart4<T1, T2, T3, T4, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer4<T1, T2, T3, T4, F>,
) : RenderingPart4<T1, T2, T3, T4, F> {
    override val renderer = renderer
    override val format: F by format
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4) {
        collector += renderer.render(data1, data2, data3, data4, format)
    }
}

internal class SimpleRenderingPart5<T1, T2, T3, T4, T5, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>,
) : RenderingPart5<T1, T2, T3, T4, T5, F> {
    override val renderer = renderer
    override val format: F by format
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5) {
        collector += renderer.render(data1, data2, data3, data4, data5, format)
    }
}

internal class SimpleRenderingPart6<T1, T2, T3, T4, T5, T6, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>,
) : RenderingPart6<T1, T2, T3, T4, T5, T6, F> {
    override val renderer = renderer
    override val format: F by format
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, data6: T6) {
        collector += renderer.render(data1, data2, data3, data4, data5, data6, format)
    }
}