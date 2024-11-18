@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.config.configurate.DamageTypeSerializer
import cc.mewcraft.wakame.config.configurate.EntityTypeSerializer
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.element.ElementSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
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
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Constraint
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.util.NamingSchemes
import java.io.File
import java.lang.reflect.Type
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object EntityAttackMappings : Initializable, KoinComponent {
    private const val ENTITY_ATTACK_MAPPINGS_CONFIG_FILE = "damage/entity_attack_mappings.yml"
    private val LOGGER: Logger = get()
    private val MAPPINGS: Reference2ObjectOpenHashMap<EntityType, List<EntityAttackMapping>> = Reference2ObjectOpenHashMap()

    /**
     * 获取某一伤害情景下原版生物的伤害映射.
     * 返回空表示未指定该情景下的伤害映射.
     */
    fun find(damager: LivingEntity, event: EntityDamageEvent): EntityAttackMapping? {
        val entityAttackMappings = MAPPINGS[damager.type] ?: return null
        for (entityAttackMapping in entityAttackMappings) {
            if (entityAttackMapping.match(event)) return entityAttackMapping
        }
        return null
    }

    override fun onPostWorld(): Unit = loadConfig()
    override fun onReload(): Unit = loadConfig()

    private fun loadConfig() {
        MAPPINGS.clear()

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
                kregister(EntityAttackMappingSerializer)
            }
        }.build().load()

        val entityTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE)
        root.childrenMap()
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
                        node.get<EntityAttackMapping>() ?: run {
                            LOGGER.warn("Malformed entity attack mapping at: ${node.path()}. Please correct your config.")
                            return@forEach
                        }
                    }

                MAPPINGS[entityType] = mappings
            }
    }
}

/**
 * 一个特定攻击场景的伤害映射.
 * [predicates] 检查场景.
 * [damageMetadataSerializable] 取得 [DamageMetadata].
 */
data class EntityAttackMapping(
    val predicates: List<DamagePredicate>,
    val damageMetadataSerializable: DamageMetadataSerializable<*>
) : KoinComponent {
    private val attributeMapAccess: AttributeMapAccess = get()

    /**
     * 检查传入的 [event] 是否与此映射相匹配.
     */
    fun match(event: EntityDamageEvent): Boolean {
        predicates.forEach {
            if (!it.test(event)) return false
        }
        return true
    }

    /**
     * 生成一个反映了此映射的 [DamageMetadata] 实例.
     */
    fun generateDamageMetadata(damager: LivingEntity, event: EntityDamageEvent): DamageMetadata {
        when (damageMetadataSerializable) {
            is DirectDamageMetadataSerializable -> {
                return damageMetadataSerializable.decode()
            }

            is VanillaValueDamageMetadataSerializable -> {
                return damageMetadataSerializable.decode(event.damage)
            }

            is AttributeMapDamageMetadataSerializable -> {
                return damageMetadataSerializable.decode(attributeMapAccess.get(damager).getOrElse {
                    error("Failed to generate damage metadata because the entity does not have an attribute map.")
                })
            }

            is MolangDamageMetadataSerializable -> {
                TODO("暂未支持Molang")
            }
        }
    }
}

internal object EntityAttackMappingSerializer : TypeSerializer<EntityAttackMapping> {

    override fun deserialize(type: Type, node: ConfigurationNode): EntityAttackMapping {
        val predicates = node.node("predicates").getList<DamagePredicate>(emptyList())
        //TODO 写的很幽默的序列化
        val damageMetadataSerializable = if (node.hasChild("direct")) {
            node.node("direct").krequire<DirectDamageMetadataSerializable>()
        } else if (node.hasChild("vanilla")) {
            node.node("vanilla").krequire<VanillaValueDamageMetadataSerializable>()
        } else if (node.hasChild("attribute")) {
            node.node("attribute").krequire<AttributeMapDamageMetadataSerializable>()
        } else {
            throw SerializationException("Can't serialize damage metadata")
        }
        return EntityAttackMapping(predicates, damageMetadataSerializable)
    }

    override fun serialize(type: Type, obj: EntityAttackMapping?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}