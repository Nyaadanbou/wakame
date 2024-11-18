package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.damage.DirectDamageMetadataSerializable
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.kregister
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
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Constraint
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.util.NamingSchemes
import java.io.File

/**
 * 记录弹射物类型映射到萌芽伤害的逻辑.
 */
@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object ProjectileTypeMappings : Initializable, KoinComponent {
    private const val PROJECTILE_TYPE_MAPPINGS_CONFIG_FILE = "damage/projectile_type_mappings.yml"

    private val MAPPINGS: Reference2ObjectOpenHashMap<EntityType, DirectDamageMetadataSerializable> = Reference2ObjectOpenHashMap()

    fun find(entityType: EntityType): DirectDamageMetadataSerializable? {
        return MAPPINGS[entityType]
    }

    override fun onPostWorld(): Unit = loadConfig()
    override fun onReload(): Unit = loadConfig()

    private fun loadConfig() {
        MAPPINGS.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(PROJECTILE_TYPE_MAPPINGS_CONFIG_FILE).bufferedReader() }
            serializers {
                registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder()
                        .defaultNamingScheme(NamingSchemes.SNAKE_CASE)
                        .addNodeResolver(NodeResolver.nodeKey())
                        .addConstraint(Required::class.java, Constraint.required())
                        .addDiscoverer(dataClassFieldDiscoverer())
                        .build()
                )
                kregister(ElementSerializer)
            }
        }.build().load()

        val entityTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE)
        root.childrenMap()
            .mapKeys { (key, _) ->
                NamespacedKey.minecraft(key.toString())
            }
            .forEach { (key, node) ->
                val entityType = entityTypeRegistry.get(key) ?: run {
                    logger.warn("Unknown entity type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mapping = node.get<DirectDamageMetadataSerializable>() ?: run {
                    logger.warn("Malformed damage metadata at: ${node.path()}. Please correct your config.")
                    return@forEach
                }
                MAPPINGS[entityType] = mapping
            }
    }

    private val logger: Logger by inject()
}