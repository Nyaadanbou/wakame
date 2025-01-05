package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.DamageTypeSerializer
import cc.mewcraft.wakame.config.configurate.EntityTypeSerializer
import cc.mewcraft.wakame.damage.DamageMetadataBuilderSerializer
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadableFun
import cc.mewcraft.wakame.reloader.ReloadableOrder
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
import org.spongepowered.configurate.util.NamingSchemes
import java.io.File

/**
 * 依据直接伤害实体的类型来获取萌芽伤害的映射.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload(
    order = ReloadableOrder.NORMAL,
    runAfter = [ElementRegistry::class]
)
//@PostWorldDependency(
//    runBefore = [ElementRegistry::class]
//)
//@ReloadDependency(
//    runBefore = [ElementRegistry::class]
//)
object DirectEntityTypeMappings : KoinComponent {
    private const val DIRECT_ENTITY_TYPE_MAPPINGS_CONFIG_FILE = "damage/direct_entity_type_mappings.yml"

    private val logger: Logger = get()
    private val noCauseMappings: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()
    private val playerMappings: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    fun byNoCauseEvent(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = noCauseMappings[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    fun byPlayerEvent(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = playerMappings[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    @InitFun
    private fun onPostWorld() {
        loadConfig()
    }

    @ReloadableFun
    private fun onReload() {
        loadConfig()
    }

    private fun loadConfig() {
        noCauseMappings.clear()
        playerMappings.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(DIRECT_ENTITY_TYPE_MAPPINGS_CONFIG_FILE).bufferedReader() }
            serializers {
                registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder()
                        .defaultNamingScheme(NamingSchemes.SNAKE_CASE)
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
                    logger.warn("Unknown entity type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mappings = node.childrenMap()
                    .map { (_, node) ->
                        node.get<DamageMapping>() ?: run {
                            logger.warn("Malformed damage mapping at: ${node.path()}. Please correct your config.")
                            return@forEach
                        }
                    }

                noCauseMappings[entityType] = mappings
            }

        root.node("player").childrenMap()
            .mapKeys { (key, _) ->
                NamespacedKey.minecraft(key.toString())
            }
            .forEach { (key, node) ->
                val entityType = entityTypeRegistry.get(key) ?: run {
                    logger.warn("Unknown entity type: ${key.asString()}. Skipped.")
                    return@forEach
                }
                val mappings = node.childrenMap()
                    .map { (_, node) ->
                        node.get<DamageMapping>() ?: run {
                            logger.warn("Malformed damage mapping at: ${node.path()}. Please correct your config.")
                            return@forEach
                        }
                    }

                playerMappings[entityType] = mappings
            }
    }
}