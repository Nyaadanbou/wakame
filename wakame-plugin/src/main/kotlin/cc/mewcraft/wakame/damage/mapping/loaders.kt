@file:JvmName("DamageMappings")
@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mapping

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.DamageMetadataBuilder
import cc.mewcraft.wakame.damage.DirectCriticalStrikeMetadataBuilder
import cc.mewcraft.wakame.damage.RawDamageContext
import cc.mewcraft.wakame.damage.VanillaDamageMetadataBuilder
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.io.File

// ------------
// 从配置文件加载 mappings
// ------------

private const val DATA_DIR = "damage"

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object DamageTypeDamageMappings {

    /**
     * [DamageTypeDamageMappings] 配置文件位置.
     */
    private val MAPPING_FILE: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("damage_type_mappings.yml").toFile()

    /**
     * 默认的 [DamageTypeMapper], 当没有为某个伤害类型指定映射时使用.
     */
    private val DEFAULT_MAPPER: DamageTypeMapper = DamageTypeMapper(
        VanillaDamageMetadataBuilder(
            criticalStrikeMetadata = DirectCriticalStrikeMetadataBuilder(),
            element = BuiltInRegistries.ELEMENT.getDefaultEntry()
        )
    )

    /**
     * 映射表, 可以动态更新.
     */
    private val mapping: Reference2ObjectOpenHashMap<DamageType, DamageTypeMapper> = Reference2ObjectOpenHashMap()

    fun get(damageType: DamageType): DamageMapper {
        return mapping[damageType] ?: DEFAULT_MAPPER
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
        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.serializer())
            }
        }.buildAndLoadString(MAPPING_FILE.readText())
        mapping.clear()
        mapping.putAll(
            rootNode.require<Map<DamageType, DamageTypeMapper>>()
        )
    }
}

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object AttackCharacteristicDamageMappings {

    /**
     * [AttackCharacteristicDamageMappings] 配置文件位置.
     */
    private val MAPPING_FILE: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("attack_characteristics_mappings.yml").toFile()

    /**
     * 映射表, 可以动态更新.
     */
    private val mapping: Reference2ObjectOpenHashMap<EntityType, List<DamagePredicateMapper>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    fun get(context: RawDamageContext): DamageMapper? {
        val entityType = context.damageSource.causingEntity?.type ?: error("Context has no causing entity.")
        val allMappers = mapping[entityType] ?: run { LOGGER.warn("No damage mappers found for entity type $entityType, returning null"); return null }
        val matchedOne = allMappers.firstOrNull { mapper -> mapper.match(context) }
        return matchedOne
    }

    private fun loadDataIntoRegistry() {
        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.serializer())
                register<DamagePredicateMapper>(DamagePredicateMapper.serializer())
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.serializer())
            }
        }.buildAndLoadString(MAPPING_FILE.readText())
        mapping.clear()
        mapping.putAll(
            rootNode.require<Map<EntityType, Map<String, DamagePredicateMapper>>>()
                .mapValues { (_, v) -> v.values.toList() }
        )
    }
}

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object NullCausingDamageMappings {

    /**
     * [NullCausingDamageMappings] 配置文件位置.
     */
    private val MAPPING_FILE: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("null_causing_mappings.yml").toFile()

    /**
     * 映射表, 可以动态更新.
     */
    private val mapping: Reference2ObjectOpenHashMap<EntityType, List<DamagePredicateMapper>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    fun get(context: RawDamageContext): DamageMapper? {
        val damageSource = context.damageSource
        val entityType = damageSource.directEntity?.type ?: error("Context has no direct entity. This is a misuse of mappings.")
        require(damageSource.causingEntity == null) { "Context *has* causing entity. This is a misuse of mappings." }
        val allMappers = mapping[entityType] ?: run { LOGGER.warn("No damage mappers found for entity type $entityType, returning null."); return null }
        val matchedOne = allMappers.firstOrNull { mapper -> mapper.match(context) }
        return matchedOne
    }

    private fun loadDataIntoRegistry() {
        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.serializer())
                register<DamagePredicateMapper>(DamagePredicateMapper.serializer())
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.serializer())
            }
        }.buildAndLoadString(MAPPING_FILE.readText())
        mapping.clear()
        mapping.putAll(
            rootNode.require<Map<EntityType, Map<String, DamagePredicateMapper>>>()
                .mapValues { (_, v) -> v.values.toList() }
        )
    }
}

/**
 * 关于该映射的具体作用请参考配置文件的注释.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object PlayerAdhocDamageMappings {

    /**
     * [PlayerAdhocDamageMappings] 配置文件位置.
     */
    private val MAPPING_FILE: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("player_adhoc_mappings.yml").toFile()

    /**
     * 映射表, 可以动态更新.
     */
    private val mapping: Reference2ObjectOpenHashMap<EntityType, List<DamagePredicateMapper>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    fun get(context: RawDamageContext): DamageMapper? {
        val damageSource = context.damageSource
        val entityType = damageSource.directEntity?.type ?: error("Context has no direct entity. This is a misuse of mappings.")
        require(damageSource.causingEntity is Player) { "Context has no causing *player*. This is a misuse of mappings." }
        val allMappers = mapping[entityType] ?: run { LOGGER.warn("No damage mappers found for entity type $entityType, returning null."); return null }
        val matchedOne = allMappers.firstOrNull { mapper -> mapper.match(context) }
        return matchedOne
    }

    private fun loadDataIntoRegistry() {
        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.serializer())
                register<DamagePredicateMapper>(DamagePredicateMapper.serializer())
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.serializer())
            }
        }.buildAndLoadString(MAPPING_FILE.readText())
        mapping.clear()
        mapping.putAll(
            rootNode.require<Map<EntityType, Map<String, DamagePredicateMapper>>>()
                .mapValues { (_, v) -> v.values.toList() }
        )
    }
}