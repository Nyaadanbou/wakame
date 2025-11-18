/**
 * 关于各种渲染器的共同实现.
 */
package cc.mewcraft.wakame.item.display.implementation

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.item.display.*
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.requireNotNull

/* 这里定义了可以在不同渲染器之间通用的 ItemRenderer 实现 */

internal abstract class AbstractItemRenderer<in C> : ItemRenderer<ItemStack, C> {
    /**
     * 渲染器的名字, 用来定位配置文件和生成日志.
     */
    abstract val name: String

    /**
     * 渲染格式.
     */
    abstract val formats: AbstractRendererFormatRegistry

    /**
     * 渲染布局.
     */
    abstract val layout: AbstractRendererLayout

    /**
     * 初始化该渲染器.
     */
    protected fun loadDataFromConfigs() {
        val renderersDirectory = KoishDataPaths.CONFIGS.resolve(ItemRendererConstants.DATA_DIR)
        val formatPath = renderersDirectory.resolve(name).resolve(ItemRendererConstants.FORMAT_FILE_NAME)
        val layoutPath = renderersDirectory.resolve(name).resolve(ItemRendererConstants.LAYOUT_FILE_NAME)
        initialize(formatPath, layoutPath)
    }
}

/* RenderingHandlerRegistry: 包含通用的代码 */

/**
 * 这个类在设计上应该使用 `object class` 实现, 然后使用 [configure]
 * 系列函数创建 [RenderingHandler] 实例并将其声明为 `val`, 并且附带上
 * [JvmField] 的注解 (避免函数调用).
 *
 * 具体的推荐用法, 请参考已经存在的实现.
 */
internal abstract class RenderingHandlerRegistry(
    private val renderer: AbstractItemRenderer<*>,
) {
    /**
     * To explicitly initialize static block.
     */
    fun bootstrap() = Unit

    /**
     * @param id 用来定位配置文件中的节点, 必须唯一
     * @param block 将数据渲染成 [IndexedText] 的逻辑
     */
    inline fun <T, reified F : RendererFormat> configure(id: String, block: IndexedDataRenderer<T, F>): RenderingHandler<T, F> {
        return RenderingHandler(provideParams<F>(id).format, block)
    }

    inline fun <T1, T2, reified F : RendererFormat> configure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): RenderingHandler2<T1, T2, F> {
        return RenderingHandler2(provideParams<F>(id).format, block)
    }

    inline fun <T1, T2, T3, reified F : RendererFormat> configure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): RenderingHandler3<T1, T2, T3, F> {
        return RenderingHandler3(provideParams<F>(id).format, block)
    }

    inline fun <T1, T2, T3, T4, reified F : RendererFormat> configure4(id: String, block: IndexedDataRenderer4<T1, T2, T3, T4, F>): RenderingHandler4<T1, T2, T3, T4, F> {
        return RenderingHandler4(provideParams<F>(id).format, block)
    }

    inline fun <T1, T2, T3, T4, T5, reified F : RendererFormat> configure5(id: String, block: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>): RenderingHandler5<T1, T2, T3, T4, T5, F> {
        return RenderingHandler5(provideParams<F>(id).format, block)
    }

    inline fun <T1, T2, T3, T4, T5, T6, reified F : RendererFormat> configure6(id: String, block: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>): RenderingHandler6<T1, T2, T3, T4, T5, T6, F> {
        return RenderingHandler6(provideParams<F>(id).format, block)
    }

    private inline fun <reified F : RendererFormat> provideParams(id: String): HandlerParams<F> {
        val format = try {
            renderer.formats.addRendererFormat<F>(id)
            renderer.formats.getRendererFormat<F>(id)
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }.requireNotNull()

        return HandlerParams(format)
    }

    // subclasses should not use it
    data class HandlerParams<F : RendererFormat>(
        val format: Provider<F>,
    )
}

/* RenderingHandler: 渲染的一部分 */

/**
 * 聚合了 *一部分渲染* 所需要的数据和逻辑.
 *
 * @param T 被渲染的数据的类型
 * @param F 渲染格式的类型
 *
 * @param format 渲染格式
 * @param renderer 渲染逻辑
 */
internal class RenderingHandler<T, F : RendererFormat>(
    val format: Provider<F>,
    val renderer: IndexedDataRenderer<T, F>,
) {
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data: T?) {
        if (data == null) return
        collector += renderer.render(data, format.get())
    }
}

internal class RenderingHandler2<T1, T2, F : RendererFormat>(
    val format: Provider<F>,
    val renderer: IndexedDataRenderer2<T1, T2, F>,
) {
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1?, data2: T2?) {
        if (data1 == null || data2 == null) return
        collector += renderer.render(data1, data2, format.get())
    }
}

internal class RenderingHandler3<T1, T2, T3, F : RendererFormat>(
    val format: Provider<F>,
    val renderer: IndexedDataRenderer3<T1, T2, T3, F>,
) {
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1?, data2: T2?, data3: T3?) {
        if (data1 == null || data2 == null || data3 == null) return
        collector += renderer.render(data1, data2, data3, format.get())
    }
}

internal class RenderingHandler4<T1, T2, T3, T4, F : RendererFormat>(
    val format: Provider<F>,
    val renderer: IndexedDataRenderer4<T1, T2, T3, T4, F>,
) {
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1?, data2: T2?, data3: T3?, data4: T4?) {
        if (data1 == null || data2 == null || data3 == null || data4 == null) return
        collector += renderer.render(data1, data2, data3, data4, format.get())
    }
}

internal class RenderingHandler5<T1, T2, T3, T4, T5, F : RendererFormat>(
    val format: Provider<F>,
    val renderer: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>,
) {
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1?, data2: T2?, data3: T3?, data4: T4?, data5: T5?) {
        if (data1 == null || data2 == null || data3 == null || data4 == null || data5 == null) return
        collector += renderer.render(data1, data2, data3, data4, data5, format.get())
    }
}

internal class RenderingHandler6<T1, T2, T3, T4, T5, T6, F : RendererFormat>(
    val format: Provider<F>,
    val renderer: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>,
) {
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1?, data2: T2?, data3: T3?, data4: T4?, data5: T5?, data6: T6?) {
        if (data1 == null || data2 == null || data3 == null || data4 == null || data5 == null || data6 == null) return
        collector += renderer.render(data1, data2, data3, data4, data5, data6, format.get())
    }
}