@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.DamageTypeSerializer
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
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Constraint
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.util.NamingSchemes
import java.io.File
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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
                kregister(DamageTypeSerializer)
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
 * 非玩家生物的近战攻击.
 *
 * ## Node structure
 * ```yaml
 * melee:
 *   damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
@ConfigSerializable
data class MeleeAttackMapping(
    @Required
    val damageMetadata: DirectDamageMetadataSerializable,
) : EntityAttackMapping {
    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.MOB_ATTACK && damageType != DamageType.MOB_ATTACK_NO_AGGRO) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        return directEntity == causingEntity
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadata.decode()
    }
}

/**
 * 非玩家生物的近战攻击(区分幼年形态)
 *
 * ## Node structure
 * ```yaml
 * melee_age:
 *   damage_metadata: <DirectDamageMetadataSerializable>
 *   baby_damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
@ConfigSerializable
data class AgeMeleeAttackMapping(
    @Required
    val damageMetadata: DirectDamageMetadataSerializable,
    @Required
    val babyDamageMetadata: DirectDamageMetadataSerializable,
) : EntityAttackMapping {
    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.MOB_ATTACK && damageType != DamageType.MOB_ATTACK_NO_AGGRO) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        if (directEntity != causingEntity) return false
        return causingEntity is Ageable
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        val causingEntity = event.damageSource.causingEntity
        val ageable = causingEntity as? Ageable ?: throw IllegalArgumentException("'${causingEntity?.type}' is not an ageable entity.")
        return if (ageable.isAdult) {
            damageMetadata.decode()
        } else {
            babyDamageMetadata.decode()
        }
    }
}

/**
 * 非玩家生物的近战攻击(区分史莱姆尺寸)
 *
 * ## Node structure
 * ```yaml
 * melee_size:
 *   damage_metadata:
 *     1: <DirectDamageMetadataSerializable>
 *     2: <DirectDamageMetadataSerializable>
 * ```
 */
@ConfigSerializable
data class SizeMeleeAttackMapping(
    @Required
    val damageMetadata: Map<Int, DirectDamageMetadataSerializable>,
) : EntityAttackMapping, KoinComponent {
    private val logger: Logger by inject()

    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.MOB_ATTACK && damageType != DamageType.MOB_ATTACK_NO_AGGRO) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        if (directEntity != causingEntity) return false
        // MagmaCube 在 Bukkit API 下是实现了 Slime 的
        return causingEntity is Slime
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        val causingEntity = event.damageSource.causingEntity
        val slime = causingEntity as? Slime ?: throw IllegalArgumentException("'${causingEntity?.type}' is not a slime.")
        return damageMetadata[slime.size]?.decode() ?: warnAndDefault(slime.size, event.damage, causingEntity)
    }

    private fun warnAndDefault(size: Int, damage: Double, causeEntity: Entity): DamageMetadata {
        logger.warn("The '$size' size '${causeEntity.type}' entity is not config!")
        return EntityDamageMetadata(
            damageBundle = damageBundle {
                default {
                    min(damage)
                    max(damage)
                    rate(1.0)
                    defensePenetration(0.0)
                    defensePenetrationRate(0.0)
                }
            },
            criticalStrikeMetadata = CriticalStrikeMetadata.DEFAULT
        )
    }
}

/**
 * 非玩家生物的近战攻击(区分河豚状态)
 *
 * ## Node structure
 * ```yaml
 * melee_puff:
 *   damage_metadata: <DirectDamageMetadataSerializable>
 *   middle_damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
@ConfigSerializable
data class PuffStateMeleeAttackMapping(
    @Required
    val fullDamageMetadata: DirectDamageMetadataSerializable,
    @Required
    val middleDamageMetadata: DirectDamageMetadataSerializable,
) : EntityAttackMapping {
    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.MOB_ATTACK && damageType != DamageType.MOB_ATTACK_NO_AGGRO) return false
        val directEntity = damageSource.directEntity ?: return false
        val causingEntity = damageSource.causingEntity ?: return false
        if (directEntity != causingEntity) return false
        return causingEntity is PufferFish
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        val causingEntity = event.damageSource.causingEntity
        val pufferFish = causingEntity as? PufferFish ?: throw IllegalArgumentException("'${causingEntity?.type}' is not a puffer fish.")
        return when (pufferFish.puffState) {
            1 -> {
                middleDamageMetadata.decode()
            }

            2 -> {
                fullDamageMetadata.decode()
            }

            else -> {
                throw IllegalArgumentException("Why can a puffer fish cause damage in state '${pufferFish.puffState}'?")
            }
        }
    }
}

/**
 * 非玩家生物的弹射物攻击.
 *
 * ## Node structure
 * ```yaml
 * projectile:
 *   projectile_type: <EntityType>
 *   damage_metadata: <DirectDamageMetadataSerializable>
 * ```
 */
@ConfigSerializable
data class ProjectileAttackMapping(
    @Required
    val damageMetadata: DirectDamageMetadataSerializable,
    @Required
    val projectileType: EntityType,
) : EntityAttackMapping {
    override fun check(damageSource: DamageSource): Boolean {
        if (damageSource.causingEntity !is LivingEntity) return false
        val directEntity = damageSource.directEntity
        if (directEntity !is Projectile) return false
        return directEntity.type == projectileType
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        return damageMetadata.decode()
    }
}

/**
 * 非玩家生物的弹射物爆炸攻击.
 *
 * ## Node structure
 * ```yaml
 * projectile_explode:
 *   projectile_type: <EntityType>
 *   element: <string>
 *   defense_penetration: <double>
 *   defense_penetration_rate: <double>
 *   critical_strike_metadata: <DirectCriticalStrikeMetadataSerializable>
 *   damage_tags: <DamageTags>
 * ```
 */
@ConfigSerializable
data class ProjectileExplodeAttackMapping(
    @Required
    val projectileType: EntityType,
    @Required
    val element: Element,
    val defensePenetration: Double = 0.0,
    val defensePenetrationRate: Double = 0.0,
    @Required
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataSerializable,
    @Required
    @Setting(nodeFromParent = true)
    val damageTags: DirectDamageTagsSerializable,
) : EntityAttackMapping {
    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.EXPLOSION && damageType != DamageType.PLAYER_EXPLOSION) return false
        if (damageSource.causingEntity !is LivingEntity) return false
        val directEntity = damageSource.directEntity
        if (directEntity !is Projectile) return false
        return directEntity.type == projectileType
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
            damageTags = damageTags.decode()
        )
    }
}

/**
 * 自身爆炸攻击.
 *
 * ## Node structure
 * ```yaml
 * self_explode:
 *   element: <string>
 *   defense_penetration: <double>
 *   defense_penetration_rate: <double>
 *   critical_strike_metadata: <DirectCriticalStrikeMetadataSerializable>
 *   damage_tags: <DamageTags>
 * ```
 */
@ConfigSerializable
data class SelfExplodeAttackMapping(
    @Required
    val element: Element,
    val defensePenetration: Double = 1.0,
    val defensePenetrationRate: Double = 1.0,
    @Required
    val criticalStrikeMetadata: DirectCriticalStrikeMetadataSerializable,
    @Required
    @Setting(nodeFromParent = true)
    val damageTags: DirectDamageTagsSerializable
) : EntityAttackMapping {
    override fun check(damageSource: DamageSource): Boolean {
        val damageType = damageSource.damageType
        if (damageType != DamageType.EXPLOSION && damageType != DamageType.PLAYER_EXPLOSION) return false
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
            damageTags = damageTags.decode()
        )
    }
}

/**
 * 根据伤害类型的攻击.
 * 可用于守卫者尖刺和激光, 唤魔者尖牙, 监守者音爆等.
 *
 * ## Node structure
 * ```yaml
 * special_type:
 *   damage_metadata:
 *     <DamageType>: <DirectDamageMetadataSerializable>
 *     <DamageType>: <DirectDamageMetadataSerializable>
 * ```
 */
@ConfigSerializable
data class SpecialDamageTypeAttackMapping(
    @Required
    val damageMetadata: Map<DamageType, DirectDamageMetadataSerializable>
) : EntityAttackMapping, KoinComponent {
    private val logger: Logger by inject()

    override fun check(damageSource: DamageSource): Boolean {
        return damageMetadata.keys.contains(damageSource.damageType)
    }

    override fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        val causingEntity = event.damageSource.causingEntity ?: throw IllegalArgumentException("Entity shouldn't be null")
        val damageType = event.damageSource.damageType
        return damageMetadata[damageType]?.decode() ?: warnAndDefault(damageType, event.damage, causingEntity)
    }

    private fun warnAndDefault(damageType: DamageType, damage: Double, causeEntity: Entity): DamageMetadata {
        logger.warn("The damage with '$damageType' type by '${causeEntity.type}' entity is not config!")
        return EntityDamageMetadata(
            damageBundle = damageBundle {
                default {
                    min(damage)
                    max(damage)
                    rate(1.0)
                    defensePenetration(0.0)
                    defensePenetrationRate(0.0)
                }
            },
            criticalStrikeMetadata = CriticalStrikeMetadata.DEFAULT
        )
    }

}

private val TYPE_MAPPING: Map<String, KType> = mapOf(
    "melee" to typeOf<MeleeAttackMapping>(),
    "melee_age" to typeOf<AgeMeleeAttackMapping>(),
    "melee_size" to typeOf<SizeMeleeAttackMapping>(),
    "melee_puff" to typeOf<PuffStateMeleeAttackMapping>(),
    "projectile" to typeOf<ProjectileAttackMapping>(),
    "projectile_explode" to typeOf<ProjectileExplodeAttackMapping>(),
    "self_explode" to typeOf<SelfExplodeAttackMapping>(),
    "special_type" to typeOf<SpecialDamageTypeAttackMapping>(),
)

internal object EntityAttackMappingSerializer : TypeSerializer<EntityAttackMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): EntityAttackMapping {
        val key = node.key().toString()
        val kType = TYPE_MAPPING[key] ?: throw SerializationException("Unknown EntityAttackMapping type: '$key'")
        return node.krequire(kType)
    }

    override fun serialize(type: Type, obj: EntityAttackMapping?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}