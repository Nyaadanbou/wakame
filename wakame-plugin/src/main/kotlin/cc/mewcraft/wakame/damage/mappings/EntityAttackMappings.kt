@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.EntityTypeSerializer
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
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
                kregister(ElementSerializer)
                kregister(DamageInfoSerializer)
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
 * ## Node structure
 * ```yaml
 * damage:
 *   element: <string>
 *   min: <double>
 *   max: <double>
 *   defense_penetration: <double>
 *   defense_penetration_rate: <double>
 *   critical_strike_chance: <double>
 *   critical_strike_power: <double>
 * ```
 */
data class DamageInfo(
    val element: Element,
    val min: Double,
    val max: Double,
    val defensePenetration: Double,
    val defensePenetrationRate: Double,
    val criticalStrikeChance: Double,
    val criticalStrikePower: Double,
)

internal object DamageInfoSerializer : TypeSerializer<DamageInfo> {
    override fun deserialize(type: Type, node: ConfigurationNode): DamageInfo {
        val element = node.node("element").krequire<Element>()
        val min = node.node("min").krequire<Double>()
        val max = node.node("max").krequire<Double>()
        val defensePenetration = node.node("defense_penetration").getDouble(0.0)
        val defensePenetrationRate = node.node("defense_penetration_rate").getDouble(0.0)
        val criticalStrikeChance = node.node("critical_strike_chance").getDouble(0.0)
        val criticalStrikePower = node.node("critical_strike_power").getDouble(1.0)
        return DamageInfo(
            element = element,
            min = min,
            max = max,
            defensePenetration = defensePenetration,
            defensePenetrationRate = defensePenetrationRate,
            criticalStrikeChance = criticalStrikeChance,
            criticalStrikePower = criticalStrikePower
        )
    }

    override fun serialize(type: Type, obj: DamageInfo?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }

}

/**
 * 原版生物特定攻击类型的映射.
 * 例如: 近战攻击, 弹射物攻击, 近战攻击(按尺寸)等.
 */
sealed interface EntityAttackMapping {
    val damageInfo: DamageInfo

    /**
     * 检查某次原版伤害是否符合此映射
     */
    fun check(damageSource: DamageSource): Boolean
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: melee
 *   damage: <DamageInfo>
 * ```
 */
data class MeleeAttackMapping(
    override val damageInfo: DamageInfo,
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
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: melee_age
 *   baby: <boolean>
 *   damage: <DamageInfo>
 * ```
 */
data class AgeMeleeAttackMapping(
    override val damageInfo: DamageInfo,
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
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: melee_size
 *   size: <int>
 *   damage: <DamageInfo>
 * ```
 */
data class SizeMeleeAttackMapping(
    override val damageInfo: DamageInfo,
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
}

/**
 * 非玩家生物的弹射物攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: projectile
 *   projectile: <EntityType>
 *   damage: <DamageInfo>
 * ```
 */
data class ProjectileAttackMapping(
    override val damageInfo: DamageInfo,
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
}

internal object EntityAttackMappingSerializer : TypeSerializer<EntityAttackMapping> {
    override fun deserialize(type: Type, node: ConfigurationNode): EntityAttackMapping {
        val typeName = node.node("type").krequire<String>()
        when (typeName) {
            MeleeAttackMapping.TYPE_NAME -> {
                val damageInfo = node.node("damage").krequire<DamageInfo>()
                return MeleeAttackMapping(damageInfo)
            }

            AgeMeleeAttackMapping.TYPE_NAME -> {
                val damageInfo = node.node("damage").krequire<DamageInfo>()
                val isBaby = node.node("baby").krequire<Boolean>()
                return AgeMeleeAttackMapping(damageInfo, isBaby)
            }

            SizeMeleeAttackMapping.TYPE_NAME -> {
                val damageInfo = node.node("damage").krequire<DamageInfo>()
                val size = node.node("size").krequire<Int>()
                return SizeMeleeAttackMapping(damageInfo, size)
            }

            ProjectileAttackMapping.TYPE_NAME -> {
                val damageInfo = node.node("damage").krequire<DamageInfo>()
                val projectileType = node.node("projectile").krequire<EntityType>()
                return ProjectileAttackMapping(damageInfo, projectileType)
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