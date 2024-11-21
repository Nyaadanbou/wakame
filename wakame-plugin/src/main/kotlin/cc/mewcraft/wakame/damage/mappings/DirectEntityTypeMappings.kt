package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.DamageTypeSerializer
import cc.mewcraft.wakame.config.configurate.EntityTypeSerializer
import cc.mewcraft.wakame.damage.DamageMetadataBuilderSerializer
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.yamlConfig
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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
 * 依据直接伤害实体的类型来获取萌芽伤害的映射.
 */
@PostWorldDependency(
    runBefore = [ElementRegistry::class]
)
@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object DirectEntityTypeMappings : Initializable, KoinComponent {
    private const val DIRECT_ENTITY_TYPE_MAPPINGS_CONFIG_FILE = "damage/direct_entity_type_mappings.yml"
    private val LOGGER: Logger = get()
    private val NO_CAUSING_MAPPINGS: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()
    private val PLAYER_MAPPINGS: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    fun findForNoCausing(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = NO_CAUSING_MAPPINGS[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) return damageMapping
        }
        return null
    }

    fun findForPlayer(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = PLAYER_MAPPINGS[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) return damageMapping
        }
        return null
    }

    override fun onPostWorld(): Unit = loadConfig()
    override fun onReload(): Unit = loadConfig()

    private fun loadConfig() {
        NO_CAUSING_MAPPINGS.clear()
        PLAYER_MAPPINGS.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(DIRECT_ENTITY_TYPE_MAPPINGS_CONFIG_FILE).bufferedReader() }
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
                kregister(EntityTypeSerializer)
                kregister(DamageTypeSerializer)
                kregister(DamageMappingSerializer)
                kregister(DamagePredicateSerializer)
                kregister(DamageMetadataBuilderSerializer)
            }
        }.build().load()

        val entityTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE)
        root.node("no_causing").childrenMap()
            .mapKeys { (key, _) ->
                NamespacedKey.minecraft(key.toString())
            }
            .forEach { (key, node) ->
                val entityType = entityTypeRegistry.get(key) ?: run {
                    LOGGER.warn("Unknown entity type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mappings = node.childrenMap()
                    .map { (_, node) ->
                        node.get<DamageMapping>() ?: run {
                            LOGGER.warn("Malformed damage mapping at: ${node.path()}. Please correct your config.")
                            return@forEach
                        }
                    }

                NO_CAUSING_MAPPINGS[entityType] = mappings
            }

        root.node("player").childrenMap()
            .mapKeys { (key, _) ->
                NamespacedKey.minecraft(key.toString())
            }
            .forEach { (key, node) ->
                val entityType = entityTypeRegistry.get(key) ?: run {
                    LOGGER.warn("Unknown entity type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mappings = node.childrenMap()
                    .map { (_, node) ->
                        node.get<DamageMapping>() ?: run {
                            LOGGER.warn("Malformed damage mapping at: ${node.path()}. Please correct your config.")
                            return@forEach
                        }
                    }

                PLAYER_MAPPINGS[entityType] = mappings
            }
    }
}