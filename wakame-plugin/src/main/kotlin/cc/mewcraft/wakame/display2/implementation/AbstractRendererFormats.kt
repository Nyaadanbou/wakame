package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.RendererFormats
import cc.mewcraft.wakame.util.yamlConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import java.nio.file.Path
import kotlin.reflect.KType

internal abstract class AbstractRendererFormats : RendererFormats, KoinComponent {
    protected val formats = HashMap<String, RendererFormat>()
    protected val typeById: HashMap<String, KType> = HashMap()
    protected val logger = get<Logger>()

    fun initialize(formatPath: Path) {
        val factory = ObjectMapper.factoryBuilder()
            .addDiscoverer(dataClassFieldDiscoverer())
            .addNodeResolver(NodeResolver.onlyWithSetting())
            .build()
        val loader = yamlConfig {
            source { formatPath.toFile().bufferedReader() }
            serializers { registerAnnotatedObjects(factory) }
        }.build()
        val root = loader.load()
    }

    /**
     * 设置指定的 [RendererFormat].
     */
    abstract fun <T : RendererFormat> set(id: String, format: T)
}
