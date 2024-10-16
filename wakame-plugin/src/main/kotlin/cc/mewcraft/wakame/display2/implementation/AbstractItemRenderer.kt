/**
 * 关于各种渲染器的共同实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.template.ItemTemplateType
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.jetbrains.annotations.VisibleForTesting
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
    abstract val formats: AbstractRendererFormats

    /**
     * 渲染布局.
     */
    abstract val layout: AbstractRendererLayout

    /**
     * 方便函数.
     */
    protected inline fun <T> ItemTemplateMap.process(type: ItemTemplateType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }

    /**
     * 方便函数.
     */
    protected inline fun <T> ItemComponentMap.process(type: ItemComponentType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }

    @VisibleForTesting
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

/**
 * 这个类在设计上应该使用 `object class` 实现, 然后使用 [configure]
 * 系列函数创建 [RenderingPart] 实例并将其声明为 `val`, 并且附带上
 * [JvmField] 的注解 (避免函数调用).
 *
 * 具体的推荐用法, 请参考已经存在的实现.
 */
internal abstract class RenderingParts(
    private val renderer: AbstractItemRenderer<*, *>,
) {
    /**
     * To explicitly initialize static block.
     */
    fun bootstrap() = Unit

    /**
     * @param id 用来定位配置文件中的节点
     * @param block 将数据渲染成文本的逻辑
     */
    protected inline fun <T, reified F : RendererFormat> configure(id: String, block: IndexedDataRenderer<T, F>): RenderingPart<T, F> {
        return provideParams<F>(id).let { params -> RenderingPart(params.show, params.format, block) }
    }

    protected inline fun <T1, T2, reified F : RendererFormat> configure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): RenderingPart2<T1, T2, F> {
        return provideParams<F>(id).let { params -> RenderingPart2(params.show, params.format, block) }
    }

    protected inline fun <T1, T2, T3, reified F : RendererFormat> configure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): RenderingPart3<T1, T2, T3, F> {
        return provideParams<F>(id).let { params -> RenderingPart3(params.show, params.format, block) }
    }

    protected inline fun <T1, T2, T3, T4, reified F : RendererFormat> configure4(id: String, block: IndexedDataRenderer4<T1, T2, T3, T4, F>): RenderingPart4<T1, T2, T3, T4, F> {
        return provideParams<F>(id).let { params -> RenderingPart4(params.show, params.format, block) }
    }

    protected inline fun <T1, T2, T3, T4, T5, reified F : RendererFormat> configure5(id: String, block: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>): RenderingPart5<T1, T2, T3, T4, T5, F> {
        return provideParams<F>(id).let { params -> RenderingPart5(params.show, params.format, block) }
    }

    protected inline fun <T1, T2, T3, T4, T5, T6, reified F : RendererFormat> configure6(id: String, block: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>): RenderingPart6<T1, T2, T3, T4, T5, T6, F> {
        return provideParams<F>(id).let { params -> RenderingPart6(params.show, params.format, block) }
    }

    private inline fun <reified F : RendererFormat> provideParams(id: String): PartParams<F> {
        val format = try {
            renderer.formats.registerRendererFormat<F>(id)
            renderer.formats.getRendererFormatProvider<F>(id)
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }

        val show = format.map { f ->
            true
        }

        return PartParams(show, format)
    }

    // subclasses should not use it
    protected data class PartParams<F : RendererFormat>(
        val show: Provider<Boolean>,
        val format: Provider<F>,
    )
}

/* RenderingPart: 渲染的一部分 */

/**
 * 聚合了 *一部分渲染* 所需要的数据和逻辑.
 *
 * @param T 被渲染的数据的类型
 * @param F 渲染格式的类型
 *
 * @param show 是否渲染这个部分
 * @param format 渲染格式
 * @param renderer 渲染逻辑
 */
internal class RenderingPart<T, F : RendererFormat>(
    show: Provider<Boolean>,
    format: Provider<F>,
    renderer: IndexedDataRenderer<T, F>,
) {
    private val show by show
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data: T) {
        // if (!show) return
        collector += renderer.render(data, format)
    }
}

internal class RenderingPart2<T1, T2, F : RendererFormat>(
    show: Provider<Boolean>,
    format: Provider<F>,
    renderer: IndexedDataRenderer2<T1, T2, F>,
) {
    private val show by show
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2) {
        // if (!show) return
        collector += renderer.render(data1, data2, format)
    }
}

internal class RenderingPart3<T1, T2, T3, F : RendererFormat>(
    show: Provider<Boolean>,
    format: Provider<F>,
    renderer: IndexedDataRenderer3<T1, T2, T3, F>,
) {
    private val show by show
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3) {
        // if (!show) return
        collector += renderer.render(data1, data2, data3, format)
    }
}

internal class RenderingPart4<T1, T2, T3, T4, F : RendererFormat>(
    show: Provider<Boolean>,
    format: Provider<F>,
    renderer: IndexedDataRenderer4<T1, T2, T3, T4, F>,
) {
    private val show by show
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4) {
        // if (!show) return
        collector += renderer.render(data1, data2, data3, data4, format)
    }
}

internal class RenderingPart5<T1, T2, T3, T4, T5, F : RendererFormat>(
    show: Provider<Boolean>,
    format: Provider<F>,
    renderer: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>,
) {
    private val show by show
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5) {
        // if (!show) return
        collector += renderer.render(data1, data2, data3, data4, data5, format)
    }
}

internal class RenderingPart6<T1, T2, T3, T4, T5, T6, F : RendererFormat>(
    show: Provider<Boolean>,
    format: Provider<F>,
    renderer: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>,
) {
    private val show by show
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, data6: T6) {
        // if (!show) return
        collector += renderer.render(data1, data2, data3, data4, data5, data6, format)
    }
}