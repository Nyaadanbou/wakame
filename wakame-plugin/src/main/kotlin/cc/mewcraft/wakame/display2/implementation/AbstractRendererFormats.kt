package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.provider
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.RendererFormats
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
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

internal abstract class AbstractRendererFormats : RendererFormats, KoinComponent {
    protected val logger = get<Logger>()

    /**
     * 所有已加载的 [RendererFormat] 实例.
     */
    private val directFormats = HashMap<String, RendererFormat>()
    private val wrappedFormats = HashSet<Provider<RendererFormat>>()

    /**
     * 配置文件 `id` -> 类型.
     */
    private val typeById = HashMap<String, KType>()

    fun initialize(formatPath: Path) {
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

        for ((id, type) in typeById) {
            val node = root.node(id)
            if (node.virtual()) {
                logger.warn("Renderer format '$id' is not found in '${formatPath.relativeTo(get<Path>(named(PLUGIN_DATA_DIR)))}'. Fallback to default format.")
            }
            directFormats[id] = node.krequire(type) // overwrite existing
        }

        // reload all renderer formats of this renderer
        wrappedFormats.forEach { provider -> provider.update() }
    }

    fun register(id: String, type: KType): Boolean {
        check(type.isSubtypeOf(typeOf<RendererFormat>())) { "Type '$type' is not a subtype of ${RendererFormat::class.simpleName}" }
        val previous = typeById.put(id, type)
        if (previous != null) {
            logger.warn("Renderer format '$id' is already registered with type $previous")
            return false
        }
        return true
    }

    fun <F : RendererFormat> get0(id: String): Provider<F> {
        // safe unchecked casts
        val provider = provider { checkNotNull(directFormats[id]) { "directFormats['$id']" } as F }
        wrappedFormats += provider as Provider<RendererFormat>
        return provider
    }

    override fun <F : RendererFormat> get(id: String): F? {
        // safe unchecked casts
        return directFormats[id] as F?
    }

    fun <T : RendererFormat> set(id: String, format: T) {
        directFormats[id] = format
    }
}
