package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.wakame.LOGGER
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.walk

/**
 * 用于在服务端 Bootstrap 阶段获取 MythicMobs 相关的信息 (aka. 读取其配置文件).
 */
object MythicBootstrapBridge {

    private const val NAMESPACE = "mythicmobs"
    private val MM_DATA_DIRECTORY_PATH = Path.of("plugins/MythicMobs")

    //

    private var inited: Boolean = false
    private val data: HashMap<ResourceLocation, EntityType<*>> = HashMap()

    /**
     * 初始化.
     */
    fun init() {
        this.data.clear()

        // 首先读取 mobs 下的所有配置文件
        for (path in MM_DATA_DIRECTORY_PATH.resolve("mobs").walk(PathWalkOption.FOLLOW_LINKS)) {
            val read = read(path.toFile())
            this.data.putAll(read)
        }

        // 然后读取 packs/<pack_name>/mobs 下的所有配置文件
        // 这会读取 packs/ 下的每一个 <pack_name>/ 中的 mobs/ 下的所有配置文件
        for (path in MM_DATA_DIRECTORY_PATH.resolve("packs").toFile().listFiles() ?: arrayOf()) {
            val mobsDir = path.resolve("mobs")
            if (mobsDir.exists() && mobsDir.isDirectory) {
                for (path in mobsDir.toPath().walk(PathWalkOption.FOLLOW_LINKS)) {
                    val read = read(path.toFile())
                    this.data.putAll(read)
                }
            }
        }

        this.inited = true
        LOGGER.info("MythicBootstrapBridge initialized, loaded ${this.data.size} MythicMobs entity types.")
    }

    /**
     * 根据 [id] 获取对应的 NMS [EntityType].
     */
    fun getEntityType(id: ResourceLocation): EntityType<*>? {
        if (inited.not()) {
            init()
        }
        return this.data[id]
    }

    /**
     * 读取 MythicMobs 的生物配置文件, 并返回对应的 NMS [EntityType].
     *
     * 一个标准的生物配置文件结构如下:
     *
     * ```yaml
     * <id>:
     *   Type: <entity_type>
     * <id>:
     *   Type: <entity_type>
     * <id>:
     *   # ... 更多生物配置
     * ```
     *
     * 该函数会读取这个 YAML 文件, 找到所有 `<id>` 合法的生物配置, 并将其映射到对应的 NMS [EntityType].
     *
     * @param file MythicMobs 的生物配置文件
     */
    private fun read(file: File): Map<ResourceLocation, EntityType<*>> {
        val ret = HashMap<ResourceLocation, EntityType<*>>()
        val root = YamlConfigurationLoader.builder().buildAndLoadString(file.readText())

        // 遍历根节点的所有键 (每个键都是一个 <id>)
        for ((nodeKey, node) in root.childrenMap().mapKeys { (k, _) -> k.toString() }) {
            // 检查 <id> 是否是合法的命名空间, 只有合法时才处理
            if (!ResourceLocation.isValidPath(nodeKey)) {
                continue
            }
            val typeString = node.node("Type").string ?: continue
            val entityType = try {
                val rl = ResourceLocation.withDefaultNamespace(typeString.lowercase())
                val hr = BuiltInRegistries.ENTITY_TYPE.get(rl)
                if (hr.isEmpty) {
                    LOGGER.warn("Unknown entity type '$typeString' found in file: ${file.path}")
                    null
                } else {
                    hr.get().value()
                }
            } catch (e: Exception) {
                LOGGER.warn("Failed to parse entity type '$typeString' found in file: ${file.path}", e)
                null
            }
            if (entityType != null) {
                val id = ResourceLocation.fromNamespaceAndPath(NAMESPACE, nodeKey)
                ret[id] = entityType
            }
        }

        return ret
    }
}