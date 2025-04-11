@file:JvmName("DamageMappings")
@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mapping

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.BuiltInRegistries
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

    private val default: DamageTypeMapper =
        DamageTypeMapper(
            VanillaDamageMetadataBuilder(
                damageTags = DirectDamageTagsBuilder(emptyList()),
                criticalStrikeMetadata = DirectCriticalStrikeMetadataBuilder(),
                element = BuiltInRegistries.ELEMENT.getDefaultEntry()
            )
        )

    private val file: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("damage_type_mappings.yml").toFile()
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

        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            file.readText()
        )
        mappings.putAll(
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

    private val file: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("attack_characteristics_mappings.yml").toFile()
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

        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.SERIALIZER)
                register<DamagePredicateMapper>(DamagePredicateMapper.SERIALIZER)
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            file.readText()
        )
        mappings.putAll(
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

    private val file: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("null_causing_mappings.yml").toFile()
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

        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.SERIALIZER)
                register<DamagePredicateMapper>(DamagePredicateMapper.SERIALIZER)
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            file.readText()
        )
        mappings.putAll(
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

    private val file: File = KoishDataPaths.CONFIGS.resolve(DATA_DIR).resolve("player_adhoc_mappings.yml").toFile()
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

        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                register<DamagePredicate>(DamagePredicate.SERIALIZER)
                register<DamagePredicateMapper>(DamagePredicateMapper.SERIALIZER)
                registerExact<DamageMetadataBuilder<*>>(DamageMetadataBuilder.SERIALIZER)
            }
        }.buildAndLoadString(
            file.readText()
        )
        mappings.putAll(
            rootNode.require<Map<EntityType, Map<String, DamagePredicateMapper>>>()
                .mapValues { (_, v) -> v.values.toList() }
        )
    }
}