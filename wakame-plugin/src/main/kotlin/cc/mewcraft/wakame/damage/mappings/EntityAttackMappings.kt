@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.DamageTypeSerializer
import cc.mewcraft.wakame.config.configurate.EntityTypeSerializer
import cc.mewcraft.wakame.damage.DamageMetadataBuilderSerializer
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.yamlConfig
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.*
import org.spongepowered.configurate.util.NamingSchemes
import java.io.File

/**
 * 依据原版生物的攻击特征来获取萌芽伤害的映射.
 */
@PostWorldDependency(
    runBefore = [ElementRegistry::class]
)
@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object EntityAttackMappings : Initializable, KoinComponent {
    private const val ENTITY_ATTACK_MAPPINGS_CONFIG_FILE = "damage/entity_attack_mappings.yml"

    private val logger: Logger = get()
    private val mappings: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    /**
     * 获取某一伤害情景下原版生物的伤害映射.
     * 返回空表示未指定该情景下的伤害映射.
     */
    fun find(damager: LivingEntity, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = mappings[damager.type] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    override fun onPostWorld() {
        loadConfig()
    }

    override fun onReload() {
        loadConfig()
    }

    private fun loadConfig() {
        mappings.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(ENTITY_ATTACK_MAPPINGS_CONFIG_FILE).bufferedReader() }
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
        root.childrenMap()
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

                this@EntityAttackMappings.mappings[entityType] = mappings
            }
    }
}