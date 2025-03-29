@file:JvmName("DamageMappings")
@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mapping

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.ScalarSerializer

// ------------
// 从配置文件加载 mappings
// ------------

private const val DATA_DIR = "damage"

// 方便函数用于减少重复代码
private inline fun <reified K, reified V> readMapNode(
    rootNode: ConfigurationNode,
    skipError: Boolean,
): Map<K, V> {
    val resultMap = HashMap<K, V>()
    val serializer = rootNode.options().serializers().get<K>() as? ScalarSerializer<K>
    if (serializer == null) {
        throw IllegalStateException("No serializer found for ${K::class}")
    }
    for ((rawKey, node) in rootNode.childrenMap()) {
        try {
            val key = serializer.deserialize(rawKey)
            val value = node.require<V>()
            resultMap[key] = value
        } catch (_: Exception) {
            if (skipError) {
                LOGGER.error("Malformed entry at ${node.path()}. Skipped.")
                continue
            } else {
                throw IllegalStateException("No value found for key $rawKey")
            }
        }
    }
    return resultMap
}

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object DamageTypeDamageMappings {

    private val default: DamageTypeMapper =
        DamageTypeMapper(
            VanillaDamageMetadataBuilder(
                damageTags = DirectDamageTagsBuilder(emptyList()),
                criticalStrikeMetadata = DirectCriticalStrikeMetadataBuilder(),
                element = KoishRegistries.ELEMENT.getDefaultEntry()
            )
        )

    private val mappings: Reference2ObjectOpenHashMap<DamageType, DamageTypeMapper> = Reference2ObjectOpenHashMap()

    fun get(damageType: DamageType): DamageMapper {
        return mappings[damageType] ?: default
    }

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    private fun loadDataIntoRegistry() {
        mappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            KoishDataPaths.CONFIGS
                .resolve(DATA_DIR)
                .resolve("damage_type_mappings.yml")
                .toFile()
                .readText()
        )
        mappings.putAll(
            readMapNode<DamageType, DamageTypeMapper>(
                rootNode = rootNode, skipError = true
            )
        )
    }
}

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object AttackCharacteristicDamageMappings {

    private val mappings: Reference2ObjectOpenHashMap<EntityType, List<DamagePredicateMapper>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    fun get(context: DamageContext): DamageMapper? {
        val entityType = context.damageSource.directEntity?.type
            ?: error("Context has no direct entity.")
        return mappings[entityType]?.first { mapper -> mapper.match(context) }
    }

    private fun loadDataIntoRegistry() {
        mappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.SERIALIZER)
                register<DamagePredicateMapper>(DamagePredicateMapper.SERIALIZER)
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            KoishDataPaths.CONFIGS
                .resolve(DATA_DIR)
                .resolve("attack_characteristics_mappings.yml")
                .toFile()
                .readText()
        )
        mappings.putAll(
            readMapNode<EntityType, Map<String, DamagePredicateMapper>>(
                rootNode = rootNode, skipError = true
            ).mapValues { (_, v) -> v.values.toList() }
        )
    }
}

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object NullCausingDamageMappings {

    private val mappings: Reference2ObjectOpenHashMap<EntityType, List<DamagePredicateMapper>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    fun get(context: DamageContext): DamageMapper? {
        val damageSource = context.damageSource
        val entityType = damageSource.directEntity?.type
            ?: error("Context has no direct entity. This is a misuse of the mappings.")
        require(damageSource.causingEntity == null) {
            "Context *has* causing entity. This is a misuse of the mappings."
        }
        return mappings[entityType]?.first { mapper -> mapper.match(context) }
    }

    private fun loadDataIntoRegistry() {
        mappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.SERIALIZER)
                register<DamagePredicateMapper>(DamagePredicateMapper.SERIALIZER)
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            KoishDataPaths.CONFIGS
                .resolve(DATA_DIR)
                .resolve("null_causing_mappings.yml")
                .toFile().readText()
        )
        mappings.putAll(
            readMapNode<EntityType, Map<String, DamagePredicateMapper>>(
                rootNode = rootNode, skipError = true
            ).mapValues { (_, v) -> v.values.toList() }
        )
    }
}

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object PlayerAdhocDamageMappings {
    private val mappings: Reference2ObjectOpenHashMap<EntityType, List<DamagePredicateMapper>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    fun get(context: DamageContext): DamageMapper? {
        val damageSource = context.damageSource
        val entityType = damageSource.directEntity?.type
            ?: error("Context has no direct entity. This is a misuse of the mappings.")
        require(damageSource.causingEntity is Player) {
            "Context has no causing *player*. This is a misuse of the mappings."
        }
        return mappings[entityType]?.first { mapper -> mapper.match(context) }
    }

    private fun loadDataIntoRegistry() {
        mappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.SERIALIZER)
                register<DamagePredicateMapper>(DamagePredicateMapper.SERIALIZER)
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            KoishDataPaths.CONFIGS
                .resolve(DATA_DIR)
                .resolve("player_adhoc_mappings.yml")
                .toFile().readText()
        )
        mappings.putAll(
            readMapNode<EntityType, Map<String, DamagePredicateMapper>>(
                rootNode = rootNode, skipError = true
            ).mapValues { (_, v) -> v.values.toList() }
        )
    }
}