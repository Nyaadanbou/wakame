package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.DamageMetadataBuilder
import cc.mewcraft.wakame.damage.DamageMetadataBuilderSerializer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.serialization.configurate.extension.transformKeys
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

/**
 * 依据直接伤害实体的类型来获取萌芽伤害的映射.
 */
@Init(
    stage = InitStage.POST_WORLD,
)
@Reload
object DirectEntityTypeMappings {

    private val noCausingDamageMapping: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()
    private val playerDamageMapping: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    @InitFun
    fun init() = loadDataIntoRegistry()

    @ReloadFun
    fun reload() = loadDataIntoRegistry()

    fun getForNoCausing(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = noCausingDamageMapping[directEntityType] ?: return null
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
        noCausingDamageMapping.clear()
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
                .resolve(DamageMappingConstants.DATA_DIR)
                .resolve("direct_entity_type_mappings.yml")
                .toFile().readText()
        )

        processDamageMappings(rootNode, noCausingDamageMapping, "no_causing")
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