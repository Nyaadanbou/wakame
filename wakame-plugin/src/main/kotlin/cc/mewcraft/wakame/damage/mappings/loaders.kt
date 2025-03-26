@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

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
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

// ------------
// 从配置文件加载 mapping
// ------------

private const val DATA_DIR = "damage"

@ConfigSerializable
internal data class DamageTypeMapping(@Setting("damage_metadata") val builder: DamageMetadataBuilder<*>)

/**
 * 依据原版伤害类型来获取萌芽伤害的映射.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object DamageTypeMappings {

    private val default: DamageTypeMapping by lazy {
        DamageTypeMapping(
            VanillaDamageMetadataBuilder(
                damageTags = DirectDamageTagsBuilder(emptyList()),
                criticalStrikeMetadata = DirectCriticalStrikeMetadataBuilder(),
                element = KoishRegistries.ELEMENT.getDefaultEntry()
            )
        )
    }

    private val mappings: Reference2ObjectOpenHashMap<DamageType, DamageTypeMapping> = Reference2ObjectOpenHashMap()

    fun get(damageType: DamageType): DamageTypeMapping {
        return mappings[damageType] ?: default
    }

    @InitFun
    fun init() = loadDataIntoRegistry()

    @ReloadFun
    fun reload() = loadDataIntoRegistry()

    private fun loadDataIntoRegistry() {
        mappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilderSerializer)
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
            val mapping = node.get<DamageTypeMapping>()
            if (mapping == null) {
                LOGGER.error("Malformed damage type mapping at ${node.path()}. Skipped.")
                continue
            }
            mappings[damageType] = mapping
        }
    }
}

/**
 * 依据原版生物的攻击特征来获取萌芽伤害的映射.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object EntityAttackMappings {

    private val mappings: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() = loadDataIntoRegistry()

    @ReloadFun
    fun reload() = loadDataIntoRegistry()

    /**
     * 获取某一伤害情景下原版生物的伤害映射.
     * 返回空表示未指定该情景下的伤害映射.
     */
    fun get(damager: LivingEntity, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = mappings[damager.type] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    private fun loadDataIntoRegistry() {
        mappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamageMapping>(DamageMappingSerializer)
                register<DamagePredicate>(DamagePredicateSerializer)
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilderSerializer)
            }
        }.buildAndLoadString(
            KoishDataPaths.CONFIGS
                .resolve(DATA_DIR)
                .resolve("entity_attack_mappings.yml")
                .toFile()
                .readText()
        )

        val entityTypeToNode = rootNode.childrenMap()
        for ((entityType, node) in entityTypeToNode.transformKeys<EntityType>(throwIfFail = false)) {
            val damageMappingList = node.childrenMap()
                .map { (_, node) ->
                    val result: DamageMapping? = node.get<DamageMapping>()
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
 * 依据直接伤害实体的类型来获取萌芽伤害的映射.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
internal object DirectEntityTypeMappings {

    private val untrackedDamageMapping: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()
    private val playerDamageMapping: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() = loadDataIntoRegistry()

    @ReloadFun
    fun reload() = loadDataIntoRegistry()

    fun getForUntracked(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = untrackedDamageMapping[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    fun getForPlayer(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = playerDamageMapping[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    private fun loadDataIntoRegistry() {
        untrackedDamageMapping.clear()
        playerDamageMapping.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamageMapping>(DamageMappingSerializer)
                register<DamagePredicate>(DamagePredicateSerializer)
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilderSerializer)
            }
        }.buildAndLoadString(
            KoishDataPaths.CONFIGS
                .resolve(DATA_DIR)
                .resolve("direct_entity_type_mappings.yml")
                .toFile().readText()
        )

        processDamageMappings(rootNode, untrackedDamageMapping, "no_causing")
        processDamageMappings(rootNode, playerDamageMapping, "player")
    }

    private fun processDamageMappings(
        rootNode: ConfigurationNode,
        targetMap: MutableMap<EntityType, List<DamageMapping>>,
        nodeName: String
    ) {
        val entityTypeToNode = rootNode.node(nodeName).childrenMap()
        for ((entityType, node) in entityTypeToNode.transformKeys<EntityType>(throwIfFail = false)) {
            val damageMappingList = node.childrenMap().map { (_, node) ->
                val result: DamageMapping? = node.get<DamageMapping>()
                if (result == null) {
                    LOGGER.error("Malformed damage type mapping at ${node.path()}. Skipped.")
                }
                result
            }.filterNotNull()

            targetMap[entityType] = damageMappingList
        }
    }
}