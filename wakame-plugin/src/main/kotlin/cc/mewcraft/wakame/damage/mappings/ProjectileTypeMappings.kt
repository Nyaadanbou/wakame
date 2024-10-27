package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.DAMAGE_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.TypeSerializer
import java.io.File
import java.lang.reflect.Type

/**
 * 记录弹射物类型映射到萌芽伤害的逻辑.
 */
@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object ProjectileTypeMappings : Initializable, KoinComponent {

    private val DEFAULT_MAPPING: ProjectileTypeMapping by ReloadableProperty {
        ProjectileTypeMapping(
            element = ElementRegistry.DEFAULT,
            value = 1.0,
            defensePenetration = 0.0,
            defensePenetrationRate = 0.0,
        )
    }

    private val MAPPINGS: Reference2ObjectOpenHashMap<EntityType, ProjectileTypeMapping> = Reference2ObjectOpenHashMap()

    fun get(entityType: EntityType): ProjectileTypeMapping {
        return MAPPINGS[entityType] ?: DEFAULT_MAPPING
    }

    override fun onPostWorld(): Unit = loadConfig()
    override fun onReload(): Unit = loadConfig()

    private fun loadConfig() {
        MAPPINGS.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(DAMAGE_GLOBAL_CONFIG_FILE).bufferedReader() }
            serializers {
                kregister(ElementSerializer)
                kregister(ProjectileTypeMappingSerializer)
            }
        }.build().load()

        val entityTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE)
        root.node("projectile_type_mappings")
            .childrenMap()
            .mapKeys { (key, _) ->
                NamespacedKey.minecraft(key.toString())
            }
            .forEach { (key, node) ->
                val entityType = entityTypeRegistry.get(key) ?: run {
                    logger.warn("Unknown entity type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mapping = node.get<ProjectileTypeMapping>() ?: run {
                    logger.warn("Malformed projectile type mapping at: ${node.path()}. Please correct your config.")
                    return@forEach
                }
                MAPPINGS[entityType] = mapping
            }
    }

    private val logger: Logger by inject()
}

data class ProjectileTypeMapping(
    val element: Element,
    val value: Double,
    val defensePenetration: Double,
    val defensePenetrationRate: Double,
)

internal object ProjectileTypeMappingSerializer : TypeSerializer<ProjectileTypeMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): ProjectileTypeMapping {
        val element = node.node("element").krequire<Element>()
        val value = node.node("value").krequire<Double>()
        val defensePenetration = node.node("defense_penetration").getDouble(0.0)
        val defensePenetrationRate = node.node("defense_penetration_rate").getDouble(0.0)
        return ProjectileTypeMapping(element, value, defensePenetration, defensePenetrationRate)
    }

    override fun serialize(type: Type, obj: ProjectileTypeMapping?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}