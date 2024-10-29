@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.EntityTypeSerializer
import cc.mewcraft.wakame.damage.*
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
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Constraint
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.util.NamingSchemes
import java.io.File
import java.lang.reflect.Type

@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object EntityAttackMappings : Initializable, KoinComponent {
    private val LOGGER: Logger = get()
    private val MAPPINGS: Reference2ObjectOpenHashMap<EntityType, List<EntityAttackMapping>> = Reference2ObjectOpenHashMap()

    /**
     * 获取某一伤害情景下原版生物的伤害映射.
     * 返回空表示未指定该情景下的伤害映射.
     */
    fun find(damageSource: DamageSource): EntityAttackMapping? {
        val causingEntity = damageSource.causingEntity ?: return null
        val entityAttackMappings = MAPPINGS[causingEntity.type] ?: return null
        for (entityAttackMapping in entityAttackMappings) {
            if (entityAttackMapping.check(damageSource)) return entityAttackMapping
        }
        return null
    }

    override fun onPostWorld(): Unit = loadConfig()
    override fun onReload(): Unit = loadConfig()

    private fun loadConfig() {
        MAPPINGS.clear()

        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(DAMAGE_GLOBAL_CONFIG_FILE).bufferedReader() }
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
                kregister(EntityAttackMappingSerializer)
            }
        }.build().load()

        val entityTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE)
        root.node("entity_attack_mappings")
            .childrenMap()
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
 * 原版生物特定攻击类型的映射.
 * 例如: 近战攻击, 弹射物攻击, 近战攻击(按尺寸)等.
 */
sealed interface EntityAttackMapping {
    /**
     * 检查某次原版伤害是否符合此映射
     */
    fun check(damageSource: DamageSource): Boolean

    fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: melee
 *   damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
data class MeleeAttackMapping(
    val damageMetadataSerializable: DirectDamageMetadataSerializable,
) : EntityAttackMapping {
    companion object {
        const val TYPE_NAME = "melee"
    }

    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.MOB_ATTACK && damageType != DamageType.MOB_ATTACK_NO_AGGRO) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        return directEntity == causingEntity
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadataSerializable.decode()
    }
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: melee_age
 *   baby: <boolean>
 *   damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
data class AgeMeleeAttackMapping(
    val damageMetadataSerializable: DirectDamageMetadataSerializable,
    val isBaby: Boolean,
) : EntityAttackMapping {
    companion object {
        const val TYPE_NAME = "melee_age"
    }

    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.MOB_ATTACK && damageType != DamageType.MOB_ATTACK_NO_AGGRO) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        if (directEntity != causingEntity) return false
        if (causingEntity is Ageable) {
            if (causingEntity.isAdult == !isBaby) return true
        }
        return false
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadataSerializable.decode()
    }
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: melee_size
 *   size: <int>
 *   damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
data class SizeMeleeAttackMapping(
    val damageMetadataSerializable: DirectDamageMetadataSerializable,
    val size: Int,
) : EntityAttackMapping {
    companion object {
        const val TYPE_NAME = "melee_size"
    }

    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.MOB_ATTACK && damageType != DamageType.MOB_ATTACK_NO_AGGRO) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        if (directEntity != causingEntity) return false
        // MagmaCube 在 Bukkit API 下是实现了 Slime 的
        if (causingEntity is Slime) {
            if (causingEntity.size == size) return true
        }
        return false
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadataSerializable.decode()
    }
}

/**
 * 非玩家生物的弹射物攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: projectile
 *   projectile: <EntityType>
 *   damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
data class ProjectileAttackMapping(
    val damageMetadataSerializable: DirectDamageMetadataSerializable,
    val projectileType: EntityType,
) : EntityAttackMapping {
    companion object {
        const val TYPE_NAME = "projectile"
    }

    override fun check(damageSource: DamageSource): Boolean {
        if (damageSource.causingEntity !is LivingEntity) return false
        val directEntity = damageSource.directEntity
        if (directEntity is Projectile) {
            if (directEntity.type == projectileType) return true
        }
        return false
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadataSerializable.decode()
    }
}

/**
 * 自身爆炸攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: self_explode
 *   element: <string>
 *   defense_penetration: <double>
 *   defense_penetration_rate: <double>
 *   critical_strike_metadata: <DirectCriticalStrikeMetadataSerializable>
 *   damage_tags: <DamageTags>
 * ```
 */
data class SelfExplodeAttackMapping(
    val element: Element,
    val defensePenetration: Double,
    val defensePenetrationRate: Double,
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataSerializable,
    val damageTagsSerializable: DirectDamageTagsSerializable
) : EntityAttackMapping {
    companion object {
        const val TYPE_NAME = "self_explode"
    }

    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.EXPLOSION) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        return directEntity == causingEntity
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return EntityDamageMetadata(
            damageBundle = damageBundle {
                single(element) {
                    min(event.damage)
                    max(event.damage)
                    rate(1.0)
                    defensePenetration(defensePenetration)
                    defensePenetrationRate(defensePenetrationRate)
                }
            },
            criticalStrikeMetadata = criticalStrikeMetadata.decode(),
            damageTags = damageTagsSerializable.decode()
        )
    }
}

/**
 * 根据伤害类型的攻击.
 * 可用于守卫者尖刺和激光, 唤魔者尖牙, 监守者音爆等.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: damage_type
 *   damage_type: <DamageType>
 *   damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
data class DamageTypeAttackMapping(
    val damageMetadataSerializable: DirectDamageMetadataSerializable,
    val damageType: DamageType
) : EntityAttackMapping {
    companion object {
        const val TYPE_NAME = "damage_type"
    }

    override fun check(damageSource: DamageSource): Boolean {
        return damageSource.damageType == damageType
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadataSerializable.decode()
    }


}

internal object EntityAttackMappingSerializer : TypeSerializer<EntityAttackMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): EntityAttackMapping {
        val typeName = node.node("type").krequire<String>()
        when (typeName) {
            MeleeAttackMapping.TYPE_NAME -> {
                val damageMetadataSerializable = node.node("damage_metadata").krequire<DirectDamageMetadataSerializable>()
                return MeleeAttackMapping(damageMetadataSerializable)
            }

            AgeMeleeAttackMapping.TYPE_NAME -> {
                val damageMetadataSerializable = node.node("damage_metadata").krequire<DirectDamageMetadataSerializable>()
                val isBaby = node.node("baby").krequire<Boolean>()
                return AgeMeleeAttackMapping(damageMetadataSerializable, isBaby)
            }

            SizeMeleeAttackMapping.TYPE_NAME -> {
                val damageMetadataSerializable = node.node("damage_metadata").krequire<DirectDamageMetadataSerializable>()
                val size = node.node("size").krequire<Int>()
                return SizeMeleeAttackMapping(damageMetadataSerializable, size)
            }

            ProjectileAttackMapping.TYPE_NAME -> {
                val damageMetadataSerializable = node.node("damage_metadata").krequire<DirectDamageMetadataSerializable>()
                val projectileType = node.node("projectile").krequire<EntityType>()
                return ProjectileAttackMapping(damageMetadataSerializable, projectileType)
            }

            SelfExplodeAttackMapping.TYPE_NAME -> {
                val element = node.node("element").krequire<Element>()
                val defensePenetration = node.node("defense_penetration").getDouble(0.0)
                val defensePenetrationRate = node.node("defense_penetration_rate").getDouble(0.0)
                val criticalStrikeMetadataSerializable = node.node("critical_strike_metadata").krequire<DirectCriticalStrikeMetadataSerializable>()
                val damageTagsSerializable = node.node("damage_tags").krequire<DirectDamageTagsSerializable>()
                return SelfExplodeAttackMapping(element, defensePenetration, defensePenetrationRate, criticalStrikeMetadataSerializable, damageTagsSerializable)
            }

            DamageTypeAttackMapping.TYPE_NAME -> {
                val damageMetadataSerializable = node.node("damage_metadata").krequire<DirectDamageMetadataSerializable>()
                val damageType = node.node("damage_type").krequire<DamageType>()
                return DamageTypeAttackMapping(damageMetadataSerializable, damageType)
            }

            else -> {
                throw SerializationException("Unknown EntityAttackMapping type")
            }
        }
    }

    override fun serialize(type: Type, obj: EntityAttackMapping?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}