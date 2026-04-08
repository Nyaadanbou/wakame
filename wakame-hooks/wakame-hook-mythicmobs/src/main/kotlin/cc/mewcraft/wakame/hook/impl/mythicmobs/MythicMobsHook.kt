package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.bridge.MythicMobsBridge
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.ConfigListener
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.DamageListener
import cc.mewcraft.wakame.hook.impl.mythicmobs.listener.ReloadListener
import cc.mewcraft.wakame.hook.impl.mythicmobs.placeholder.KoishPlaceholders
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.skill.SkillIntegration
import cc.mewcraft.wakame.util.registerEvents
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.constants.MobKeys
import net.kyori.adventure.key.Key
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EntityType
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.walk

@Hook(plugins = ["MythicMobs"])
object MythicMobsHook {
    init {
        // 注册 Listeners
        ConfigListener.registerEvents()
        DamageListener.registerEvents()
        ReloadListener.registerEvents()

        // 注册 Placeholders
        KoishPlaceholders.register(MythicBukkit.inst())

        MythicMobsBridge.setImplementation(MythicMobsBridgeImpl)
        SkillIntegration.setImplementation(MythicSkillIntegration)

        // 目前的所有实现暂时不需要获取 MythicMobs 的怪物的 id, 等之后需要的时候再把这个注释给去掉.
        // BuiltInRegistries.ENTITY_REF_LOOKUP_DIR.add("mythicmobs", MythicMobsEntityRefLookupDictionary())
    }
}

private object MythicMobsBridgeImpl : MythicMobsBridge {
    private const val NAMESPACE = "mythicmobs"
    private val MM_DATA_DIRECTORY_PATH = Path.of("plugins/MythicMobs")
    private var inited: Boolean = false
    private val data: HashMap<Identifier, EntityType<*>> = HashMap()

    override fun initBootstrapper() {
        data.clear()

        // 首先读取 mobs 下的所有配置文件
        for (path in MM_DATA_DIRECTORY_PATH.resolve("mobs").walk(PathWalkOption.FOLLOW_LINKS)) {
            val read = extractEntityTypeMap(path.toFile())
            data.putAll(read)
        }

        // 然后读取 packs/<pack_name>/mobs 下的所有配置文件
        // 这会读取 packs/ 下的每一个 <pack_name>/ 中的 mobs/ 下的所有配置文件
        for (path in MM_DATA_DIRECTORY_PATH.resolve("packs").toFile().listFiles() ?: arrayOf()) {
            val mobsDir = path.resolve("mobs")
            if (mobsDir.exists() && mobsDir.isDirectory) {
                for (path in mobsDir.toPath().walk(PathWalkOption.FOLLOW_LINKS)) {
                    val read = extractEntityTypeMap(path.toFile())
                    data.putAll(read)
                }
            }
        }

        inited = true
        LOGGER.info("MythicBootstrapBridge initialized, loaded ${data.size} MythicMobs entity types: ${data.entries.joinToString(prefix = "[", postfix = "]", transform = { "${it.key.path}: ${it.value.toShortString()}" })}.")
    }

    override fun getRealEntityType(id: Identifier): EntityType<*>? {
        if (inited.not())
            initBootstrapper()
        return data[id]
    }

    override fun writeIdMark(entity: Entity, id: Key) {
        entity.persistentDataContainer.set(MobKeys.TYPE, PersistentDataType.STRING, id.value())
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
    private fun extractEntityTypeMap(file: File): Map<Identifier, EntityType<*>> {
        val ret = HashMap<Identifier, EntityType<*>>()
        val root = YamlConfigurationLoader.builder().buildAndLoadString(file.readText())

        // 遍历根节点的所有键 (每个键都是一个 <id>)
        for ((nodeKey, node) in root.childrenMap().mapKeys { (k, _) -> k.toString() }) {
            // 检查 <id> 是否是合法的命名空间, 只有合法时才处理
            if (!Identifier.isValidPath(nodeKey)) {
                continue
            }
            val typeString = node.node("Type").string ?: continue
            val entityType = try {
                val rl = Identifier.withDefaultNamespace(typeString.lowercase())
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
                val id = Identifier.fromNamespaceAndPath(NAMESPACE, nodeKey)
                ret[id] = entityType
            }
        }

        return ret
    }
}