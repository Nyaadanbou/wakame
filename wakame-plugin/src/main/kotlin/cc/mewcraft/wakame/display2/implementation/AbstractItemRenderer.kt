/**
 * 关于各种渲染器的共同实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.display2.IndexedDataRenderer
import cc.mewcraft.wakame.display2.IndexedDataRenderer2
import cc.mewcraft.wakame.display2.IndexedDataRenderer3
import cc.mewcraft.wakame.display2.IndexedDataRenderer4
import cc.mewcraft.wakame.display2.IndexedDataRenderer5
import cc.mewcraft.wakame.display2.IndexedDataRenderer6
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.ItemRenderer
import cc.mewcraft.wakame.display2.ItemRendererConstants
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.getMeta
import cc.mewcraft.wakame.item2.getProperty
import io.papermc.paper.datacomponent.DataComponentType
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.VisibleForTesting
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

    // 方便函数

    protected inline fun <T : Any> ItemStack.process(type: DataComponentType.Valued<T>, block: (T) -> Unit) {
        getData(type)?.apply(block)
    }

    protected inline fun <T1 : Any, T2 : Any> ItemStack.process(type1: DataComponentType.Valued<T1>, type2: DataComponentType.Valued<T2>, block: (T1?, T2?) -> Unit) {
        block(getData(type1), getData(type2))
    }

    protected inline fun <T1 : Any, T2 : Any, T3 : Any> ItemStack.process(type1: DataComponentType.Valued<T1>, type2: DataComponentType.Valued<T2>, type3: DataComponentType.Valued<T3>, block: (T1?, T2?, T3?) -> Unit) {
        block(getData(type1), getData(type2), getData(type3))
    }

    // 方便函数

    protected inline fun <T> ItemStack.process(type: ItemPropertyType<T>, block: (T) -> Unit) {
        getProperty(type)?.apply(block)
    }

    protected inline fun <T1, T2> ItemStack.process(type1: ItemPropertyType<T1>, type2: ItemPropertyType<T2>, block: (T1?, T2?) -> Unit) {
        block(getProperty(type1), getProperty(type2))
    }

    protected inline fun <T1, T2, T3> ItemStack.process(type1: ItemPropertyType<T1>, type2: ItemPropertyType<T2>, type3: ItemPropertyType<T3>, block: (T1?, T2?, T3?) -> Unit) {
        block(getProperty(type1), getProperty(type2), getProperty(type3))
    }

    // 方便函数

    protected inline fun <T> ItemStack.process(type: ItemDataType<T>, block: (T) -> Unit) {
        getData(type)?.apply(block)
    }

    protected inline fun <T1, T2> ItemStack.process(type1: ItemDataType<T1>, type2: ItemDataType<T2>, block: (T1?, T2?) -> Unit) {
        block(getData(type1), getData(type2))
    }

    protected inline fun <T1, T2, T3> ItemStack.process(type1: ItemDataType<T1>, type2: ItemDataType<T2>, type3: ItemDataType<T3>, block: (T1?, T2?, T3?) -> Unit) {
        block(getData(type1), getData(type2), getData(type3))
    }

    // 方便函数

    protected inline fun <U : ItemMetaEntry<V>, V> ItemStack.process(type: ItemMetaType<U, V>, block: (U) -> Unit) {
       getMeta(type)?.apply(block)
    }

    protected inline fun <U1 : ItemMetaEntry<V1>, U2 : ItemMetaEntry<V2>, V1, V2> ItemStack.process(type1: ItemMetaType<U1, V1>, type2: ItemMetaType<U2, V2>, block: (U1?, U2?) -> Unit) {
        block(getMeta(type1), getMeta(type2))
    }

    protected inline fun <U1 : ItemMetaEntry<V1>, U2 : ItemMetaEntry<V2>, U3 : ItemMetaEntry<V3>, V1, V2, V3> ItemStack.process(type1: ItemMetaType<U1, V1>, type2: ItemMetaType<U2, V2>, type3: ItemMetaType<U3, V3>, block: (U1?, U2?, U3?) -> Unit) {
        block(getMeta(type1), getMeta(type2), getMeta(type3))
    }

    @VisibleForTesting
    fun loadDataFromConfigs() {
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
    protected data class HandlerParams<F : RendererFormat>(
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
    format: Provider<F>,
    renderer: IndexedDataRenderer<T, F>,
) {
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data: T) {
        collector += renderer.render(data, format)
    }
}

internal class RenderingHandler2<T1, T2, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer2<T1, T2, F>,
) {
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2) {
        collector += renderer.render(data1, data2, format)
    }
}

internal class RenderingHandler3<T1, T2, T3, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer3<T1, T2, T3, F>,
) {
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3) {
        collector += renderer.render(data1, data2, data3, format)
    }
}

internal class RenderingHandler4<T1, T2, T3, T4, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer4<T1, T2, T3, T4, F>,
) {
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4) {
        collector += renderer.render(data1, data2, data3, data4, format)
    }
}

internal class RenderingHandler5<T1, T2, T3, T4, T5, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer5<T1, T2, T3, T4, T5, F>,
) {
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5) {
        collector += renderer.render(data1, data2, data3, data4, data5, format)
    }
}

internal class RenderingHandler6<T1, T2, T3, T4, T5, T6, F : RendererFormat>(
    format: Provider<F>,
    renderer: IndexedDataRenderer6<T1, T2, T3, T4, T5, T6, F>,
) {
    private val format by format
    private val renderer = renderer
    fun process(collector: ReferenceOpenHashSet<IndexedText>, data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, data6: T6) {
        collector += renderer.render(data1, data2, data3, data4, data5, data6, format)
    }
}