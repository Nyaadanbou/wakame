/**
 * 关于各种渲染器的共同实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.initializer.Initializable
import org.jetbrains.annotations.TestOnly
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.nio.file.Path

/* 这里定义了可以在不同渲染器之间通用的 ItemRenderer 实现 */

internal abstract class AbstractItemRenderer<in T, in C> : ItemRenderer<T, C>, Initializable, KoinComponent {
    protected val logger = get<Logger>()

    /**
     * 渲染器的名字, 用来定位配置文件和生成日志.
     */
    abstract val name: String

    /**
     * 渲染格式.
     */
    abstract val rendererFormats: AbstractRendererFormats

    /**
     * 渲染布局.
     */
    abstract val rendererLayout: AbstractRendererLayout

    @TestOnly
    fun initialize0() {
        val renderersDirectory = get<Path>(named(PLUGIN_DATA_DIR)).resolve("renderers")
        val formatPath = renderersDirectory.resolve(name).resolve(FORMAT_FILE_NAME)
        val layoutPath = renderersDirectory.resolve(name).resolve(LAYOUT_FILE_NAME)
        initialize(formatPath, layoutPath)
    }

    override fun onPostWorld() {
        initialize0()
    }

    override fun onReload() {
        initialize0()
    }

    companion object {
        const val LAYOUT_FILE_NAME = "layout.yml"
        const val FORMAT_FILE_NAME = "formats.yml"
    }
}

/* RenderingParts: 包含通用的代码 */

internal abstract class RenderingParts(
    private val renderer: AbstractItemRenderer<*, *>,
) {
    fun bootstrap() = Unit // to explicitly initialize static block

    /**
     * @param id 用来定位配置文件中的节点
     * @param block 将数据渲染成文本的逻辑
     */
    protected inline fun <T, reified F : RendererFormat> configure(id: String, block: IndexedDataRenderer<T, F>): RenderingPart<T, F> =
        RenderingPart(provideFormat<F>(id), block)

    protected inline fun <T1, T2, reified F : RendererFormat> configure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): RenderingPart2<T1, T2, F> =
        RenderingPart2(provideFormat<F>(id), block)

    protected inline fun <T1, T2, T3, reified F : RendererFormat> configure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): RenderingPart3<T1, T2, T3, F> =
        RenderingPart3(provideFormat<F>(id), block)

    protected inline fun <T1, T2, T3, T4, reified F : RendererFormat> configure4(id: String, block: IndexedDataRenderer4<T1, T2, T3, T4, F>): RenderingPart4<T1, T2, T3, T4, F> =
        RenderingPart4(provideFormat<F>(id), block)

    protected inline fun <T1, T2, T3, T4, T5, reified F : RendererFormat> configure5(id: String, block: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>): RenderingPart5<T1, T2, T3, T4, T5, F> =
        RenderingPart5(provideFormat<F>(id), block)

    protected inline fun <T1, T2, T3, T4, T5, T6, reified F : RendererFormat> configure6(id: String, block: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>): RenderingPart6<T1, T2, T3, T4, T5, T6, F> =
        RenderingPart6(provideFormat<F>(id), block)

    private inline fun <reified F : RendererFormat> provideFormat(id: String): Provider<F> {
        try {
            val added = renderer.rendererFormats.registerRendererFormat<F>(id)
            val format = renderer.rendererFormats.getRendererFormatProvider<F>(id)
            return format
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }
    }
}

/* RenderingPart: 渲染的一部分 */

/**
 * 聚合了渲染一种物品数据所需要的数据和逻辑.
 *
 * @param T 被渲染的数据的类型
 * @param F 渲染格式的类型
 */
internal class RenderingPart<T, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer<T, F>,
) {
    private val format: F by format
    private val renderer = renderer
    fun process(collector: MutableList<IndexedText>, data: T) {
        collector += renderer.render(data, format)
    }
}

internal class RenderingPart2<T1, T2, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer2<T1, T2, F>,
) {
    private val format: F by format
    private val renderer = renderer
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2) {
        collector += renderer.render(data1, data2, format)
    }
}

internal class RenderingPart3<T1, T2, T3, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer3<T1, T2, T3, F>,
) {
    private val format: F by format
    private val renderer = renderer
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3) {
        collector += renderer.render(data1, data2, data3, format)
    }
}

internal class RenderingPart4<T1, T2, T3, T4, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer4<T1, T2, T3, T4, F>,
) {
    private val format: F by format
    private val renderer = renderer
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4) {
        collector += renderer.render(data1, data2, data3, data4, format)
    }
}

internal class RenderingPart5<T1, T2, T3, T4, T5, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>,
) {
    private val format: F by format
    private val renderer = renderer
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5) {
        collector += renderer.render(data1, data2, data3, data4, data5, format)
    }
}

internal class RenderingPart6<T1, T2, T3, T4, T5, T6, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>,
) {
    private val format: F by format
    private val renderer = renderer
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, data6: T6) {
        collector += renderer.render(data1, data2, data3, data4, data5, data6, format)
    }
}