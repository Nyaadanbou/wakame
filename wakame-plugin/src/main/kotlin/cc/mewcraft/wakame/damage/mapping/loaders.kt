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
import cc.mewcraft.wakame.serialization.configurate.extension.transformKeys
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

// ------------
// 从配置文件加载 mappings
// ------------

private const val DATA_DIR = "damage"


/**
 * 当 `DamageSource#directEntity` 为 `null` 时会使用到这个映射.
 * 原版下, 只有环境(比如岩浆)造成的伤害 `directEntity` 为 `null`.
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

        val damageTypeToNode = rootNode.childrenMap()
        for ((damageType, node) in damageTypeToNode.transformKeys<DamageType>(throwIfFail = false)) {
            val mapping = node.get<DamageTypeMapper>()
            if (mapping == null) {
                LOGGER.error("Malformed damage type mapping at ${node.path()}. Skipped.")
                continue
            }
            mappings[damageType] = mapping
        }
    }
}

/**
 * 依据原版生物的攻击特征来获取 [DamageMapper].
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
        val directEntity = context.damageSource.directEntity ?: error("Context has no direct entity.")
        return mappings[directEntity.type]?.first { mapper -> mapper.match(context) }
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

        val entityTypeToNode = rootNode.childrenMap()
        for ((entityType, node) in entityTypeToNode.transformKeys<EntityType>(throwIfFail = false)) {
            val damageMappingList = node.childrenMap()
                .map { (_, node) ->
                    val result: DamagePredicateMapper? = node.get<DamagePredicateMapper>()
                    if (result == null) {
                        LOGGER.error("Malformed damage type mapping at ${node.path()}. Skipped.")
                    }
                    result
                }
                .filterNotNull()

            mappings[entityType] = damageMappingList
        }
    }
}

/**
 * 依据 *直接伤害实体* 的类型来获取萌芽伤害的映射.
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
        val directEntity = damageSource.directEntity ?: error("Context has no direct entity. This is a misuse of the mappings.")
        require(damageSource.causingEntity == null) { "Context *has* causing entity. This is a misuse of the mappings."}
        return mappings[directEntity.type]?.first { mapper -> mapper.match(context) }
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

        readNodeThenPutIntoMap(rootNode, mappings)
    }

    private fun readNodeThenPutIntoMap(
        rootNode: ConfigurationNode,
        targetMap: MutableMap<EntityType, List<DamagePredicateMapper>>,
    ) {
        val entityTypeToNode = rootNode.childrenMap()
        for ((entityType, node) in entityTypeToNode.transformKeys<EntityType>(throwIfFail = false)) {
            val damageMappingList = node.childrenMap().map { (_, node) ->
                val result: DamagePredicateMapper? = node.get<DamagePredicateMapper>()
                if (result == null) {
                    LOGGER.error("Malformed damage type mapping at ${node.path()}. Skipped.")
                }
                result
            }.filterNotNull()

            targetMap[entityType] = damageMappingList
        }
    }
}

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
        val directEntity = damageSource.directEntity ?: error("Context has no direct entity. This is a misuse of the mappings.")
        require(damageSource.causingEntity is Player) { "Context has no causing *player*. This is a misuse of the mappings." }
        return mappings[directEntity.type]?.first { mapper -> mapper.match(context) }
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

        readNodeThenPutIntoMap(rootNode, mappings)
    }

    private fun readNodeThenPutIntoMap(
        rootNode: ConfigurationNode,
        targetMap: MutableMap<EntityType, List<DamagePredicateMapper>>,
    ) {
        val entityTypeToNode = rootNode.childrenMap()
        for ((entityType, node) in entityTypeToNode.transformKeys<EntityType>(throwIfFail = false)) {
            val damageMappingList = node.childrenMap().map { (_, node) ->
                val result: DamagePredicateMapper? = node.get<DamagePredicateMapper>()
                if (result == null) {
                    LOGGER.error("Malformed damage type mapping at ${node.path()}. Skipped.")
                }
                result
            }.filterNotNull()

            targetMap[entityType] = damageMappingList
        }
    }

}