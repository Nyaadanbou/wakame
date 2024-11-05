package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.*
import org.spongepowered.configurate.util.NamingSchemes
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.immutable.provider
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.io.path.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/* 这里定义了可以在不同渲染器之间通用的 RendererFormats 实现 */

internal abstract class AbstractRendererFormats(
    protected val renderer: AbstractItemRenderer<*, *>,
) : RendererFormats, KoinComponent {
    protected val logger = get<Logger>()

    /**
     * `配置中的节点路径` -> `typeOf<RendererFormat>`, 用于引导配置文件的序列化.
     *
     * 该映射在 [RenderingParts.bootstrap] 执行时就已经确定和冻结, 后续禁止更新!
     */
    private val rendererFormatTypeById = HashMap<String, KType>()

    /**
     * 所有已加载的 [RendererFormat] 实例.
     */
    private val registeredRendererFormats = HashMap<String, RendererFormat>()

    /**
     * 所有已加载的 [Provider] 实例, 记录引用以支持热重载渲染器.
     */
    private val registeredFormatProviders = HashMap<String, Provider<RendererFormat>>()

    /**
     * 所有已加载的 [TextMetaFactory] 实例.
     */
    val textMetaFactoryRegistry = TextMetaFactoryRegistry()

    private fun cleanup() {
        registeredRendererFormats.clear()
        textMetaFactoryRegistry.reset()
    }

    /**
     * 初始化本实例的所有状态.
     */
    fun initialize(formatPath: Path) {
        cleanup()

        val loader = yamlConfig {
            withDefaults()
            serializers {
                registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder()
                        .defaultNamingScheme(NamingSchemes.SNAKE_CASE)
                        .addNodeResolver(NodeResolver.nodeKey())
                        // .addNodeResolver(NodeResolver.onlyWithSetting())
                        .addConstraint(Required::class.java, Constraint.required())
                        .addDiscoverer(dataClassFieldDiscoverer())
                        .build()
                )
            }
        }
        val root = loader.buildAndLoadString(formatPath.readText())

        for ((id, type) in rendererFormatTypeById) {
            val node = root.node(id)
            if (node.virtual()) {
                logger.warn("Renderer format '$id' is not found in '${formatPath.relativeTo(get<Path>(named(PLUGIN_DATA_DIR)))}'")
            }
            val format = node.krequire<RendererFormat>(type)

            // will overwrite the one already existing
            registeredRendererFormats[id] = format
            // create & register the text meta factory
            textMetaFactoryRegistry.registerFactory(format.textMetaFactory)

            logger.info("Loaded renderer format (${formatPath.parent.name}): $id")
        }

        // reload all renderer formats of this renderer
        registeredFormatProviders.values.forEach { provider -> provider.update() }
    }

    inline fun <reified F : RendererFormat> registerRendererFormat(id: String): Boolean {
        val previous = rendererFormatTypeById.put(id, typeOf<F>())
        if (previous != null) {
            logger.error("Renderer format '$id' is already registered with type $previous. This is a bug!")
            return false
        }
        return true
    }

    // 必须外面套个函数来访问 registeredFormats[id] 否则
    // lambda 好像会被自动 inline ??? 有时间再摸索一下
    @Suppress("UNCHECKED_CAST")
    private fun <F : RendererFormat> getRegisteredRendererFormat(id: String): F {
        return (registeredRendererFormats[id] as F?) ?: error("Renderer format '$id' is not registered")
    }

    @Suppress("UNCHECKED_CAST")
    fun <F : RendererFormat> getRendererFormatProvider(id: String): Provider<F> {
        val provider = registeredFormatProviders.getOrPut(id) {
            provider {
                getRegisteredRendererFormat<F>(id)
            }
        }
        return provider as Provider<F>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <F : RendererFormat> get(id: String): F? {
        return registeredRendererFormats[id] as F?
    }
}
