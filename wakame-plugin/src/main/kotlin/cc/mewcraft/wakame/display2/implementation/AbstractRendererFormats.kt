package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.provider
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
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.io.path.*
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

/* 这里定义了可以在不同渲染器之间通用的 RendererFormats 实现 */

internal abstract class AbstractRendererFormats : RendererFormats, KoinComponent {
    protected val logger = get<Logger>()

    /**
     * 所有已加载的 [RendererFormat] 实例.
     */
    private val registeredFormats = HashMap<String, RendererFormat>()

    /**
     * 所有已加载的 [Provider] 实例, 记录引用以支持热重载渲染器.
     */
    private val registeredFormatProviders = HashSet<Provider<RendererFormat>>()

    /**
     * 配置文件 `id` -> 类型. 用于引导配置文件的序列化过程.
     */
    private val formatTypeById = HashMap<String, KType>()

    /**
     * 所有已加载的 [TextMetaFactory] 实例.
     */
    val textMetaFactoryRegistry = TextMetaFactoryRegistry()

    fun cleanup() {
        registeredFormats.clear()
        textMetaFactoryRegistry.clear()
    }

    /**
     * 初始化本实例的所有状态.
     */
    fun initialize(formatPath: Path) {
        cleanup()

        val factory = ObjectMapper.factoryBuilder()
            .addNodeResolver(NodeResolver.nodeKey())
            .addNodeResolver(NodeResolver.onlyWithSetting())
            .addConstraint(Required::class.java, Constraint.required())
            .addDiscoverer(dataClassFieldDiscoverer())
            .build()
        val loader = yamlConfig {
            withDefaults()
            serializers { registerAnnotatedObjects(factory) }
        }
        val root = loader.buildAndLoadString(formatPath.readText())

        for ((id, type) in formatTypeById) {
            val node = root.node(id)
            if (node.virtual()) {
                // TODO display2 当配置文件有缺省时, 支持回退到默认格式, 而不是直接抛异常
                logger.warn("Renderer format '$id' is not found in '${formatPath.relativeTo(get<Path>(named(PLUGIN_DATA_DIR)))}'. Fallback to default format.")
            }
            val format = node.krequire<RendererFormat>(type)

            // will overwrite the one already existing
            registeredFormats[id] = format
            // create & register the text meta factory
            textMetaFactoryRegistry.registerFactory(format.createTextMetaFactory()) // FIXME 传入必要的参数

            logger.info("Loaded renderer format (${formatPath.parent.name}): $id")
        }

        // reload all renderer formats of this renderer
        registeredFormatProviders.forEach { provider -> provider.update() }
    }

    fun register(id: String, type: KType): Boolean {
        check(type.isSubtypeOf(typeOf<RendererFormat>())) { "Type '$type' is not a subtype of ${RendererFormat::class.simpleName}" }
        val previous = formatTypeById.put(id, type)
        if (previous != null) {
            logger.warn("Renderer format '$id' is already registered with type $previous")
            return false
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    fun <F : RendererFormat> get0(id: String): Provider<F> {
        val provider = provider {
            val format = checkNotNull(registeredFormats[id]) { "registeredFormats['$id']" }
            return@provider format as F
        }
        registeredFormatProviders += provider as Provider<RendererFormat>
        return provider
    }

    @Suppress("UNCHECKED_CAST")
    override fun <F : RendererFormat> get(id: String): F? {
        return registeredFormats[id] as F?
    }

    fun <T : RendererFormat> set(id: String, format: T) {
        registeredFormats[id] = format
    }
}
