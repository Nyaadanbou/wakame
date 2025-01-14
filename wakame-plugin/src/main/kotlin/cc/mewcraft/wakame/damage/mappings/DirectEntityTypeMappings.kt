package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.damage.DamageMetadataBuilderSerializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.qualifier.named
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.File

/**
 * 依据直接伤害实体的类型来获取萌芽伤害的映射.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload()
object DirectEntityTypeMappings {
    private const val DIRECT_ENTITY_TYPE_MAPPINGS_CONFIG_PATH = "damage/direct_entity_type_mappings.yml"

    private val noCauseMappings: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()
    private val playerMappings: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    @InitFun
    private fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    private fun reload() {
        loadDataIntoRegistry()
    }

    fun byNoCausingEvent(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
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

    private fun loadDataIntoRegistry() {
        noCauseMappings.clear()
        playerMappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                kregister(DamageMappingSerializer)
                kregister(DamagePredicateSerializer)
                kregister(DamageMetadataBuilderSerializer)
            }
            source { Injector.get<File>(named(PLUGIN_DATA_DIR)).resolve(DIRECT_ENTITY_TYPE_MAPPINGS_CONFIG_PATH).bufferedReader() }
        }.build().load()

        val entityTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE)
        rootNode.node("no_causing").childrenMap()
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

                noCauseMappings[entityType] = mappings
            }

        rootNode.node("player").childrenMap()
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

                playerMappings[entityType] = mappings
            }
    }
}