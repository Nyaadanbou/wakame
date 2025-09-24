package cc.mewcraft.wakame.item.display.implementation

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.display.RendererFormat
import cc.mewcraft.wakame.item.display.RendererFormatRegistry
import cc.mewcraft.wakame.item.display.TextMetaFactoryRegistry
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import xyz.xenondevs.commons.provider.MutableProvider
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.mutableProvider
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/* 这里定义了可以在不同渲染器之间通用的 RendererFormats 实现 */

internal abstract class AbstractRendererFormatRegistry(
    protected val renderer: AbstractItemRenderer<*>,
) : RendererFormatRegistry {

    /**
     * `K: 配置文件中的节点路径` -> `V: typeOf<RendererFormat>`, 用于引导配置文件的序列化.
     *
     * 该映射在 [RenderingHandlerRegistry.bootstrap] 执行时就已经确定和冻结, 后续禁止更新!
     */
    private val typeIdToRendererFormatType = HashMap<String, KType>()

    /**
     * 所有已加载的 [RendererFormat] 实例.
     */
    private val registeredRendererFormatMap = HashMap<String, MutableProvider<RendererFormat?>>()

    /**
     * 所有已加载的 [TextMetaFactory] 实例.
     */
    val textMetaFactoryRegistry = TextMetaFactoryRegistry()

    /**
     * 初始化本实例的所有状态.
     */
    fun initialize(formatPath: Path) {
        textMetaFactoryRegistry.reset()

        val rootNode = yamlLoader { withDefaults() }.buildAndLoadString(formatPath.readText())
        val relativeTo = formatPath.relativeTo(KoishDataPaths.CONFIGS)
        for ((id, type) in typeIdToRendererFormatType) {
            val node = rootNode.node(id)
            if (node.virtual()) {
                LOGGER.error("Renderer format '$id' is not found in '$relativeTo'")
                continue
            }
            val format = node.require<RendererFormat>(type)

            // load or update the renderer format
            val existingRendererFormat = registeredRendererFormatMap[id]
            if (existingRendererFormat != null) {
                existingRendererFormat.set(format)
            } else {
                registeredRendererFormatMap[id] = mutableProvider(format)
            }

            // create & register the text meta factory
            textMetaFactoryRegistry.registerFactory(format.textMetaFactory, format.textMetaPredicate)
        }

        LOGGER.info("Registered ${textMetaFactoryRegistry.getKnownFactory().size} TextMetaFactory from '$relativeTo'")
    }

    inline fun <reified F : RendererFormat> addRendererFormat(id: String): Boolean {
        val previous = typeIdToRendererFormatType.put(id, typeOf<F>())
        if (previous != null) {
            LOGGER.error("Renderer format '$id' is already registered with type $previous. This is a bug!")
            return false
        }
        return true
    }

    override fun <F : RendererFormat> getRendererFormat(id: String): Provider<F> {
        return registeredRendererFormatMap.getOrPut(id) { mutableProvider(null) } as Provider<F>
    }
}
