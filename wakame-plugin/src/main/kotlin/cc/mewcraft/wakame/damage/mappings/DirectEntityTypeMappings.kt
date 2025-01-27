package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
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
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.File

/**
 * 依据直接伤害实体的类型来获取萌芽伤害的映射.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload
object DirectEntityTypeMappings {

    private val damageMappingForNoCause: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()
    private val damageMappingForPlayer: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    @InitFun
    private fun init() = loadDataIntoRegistry()

    @ReloadFun
    private fun reload() = loadDataIntoRegistry()

    fun getForNoCause(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = damageMappingForNoCause[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    fun getForPlayer(directEntityType: EntityType, event: EntityDamageEvent): DamageMapping? {
        val damageMappings = damageMappingForPlayer[directEntityType] ?: return null
        for (damageMapping in damageMappings) {
            if (damageMapping.match(event)) {
                return damageMapping
            }
        }
        return null
    }

    private fun loadDataIntoRegistry() {
        damageMappingForNoCause.clear()
        damageMappingForPlayer.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamageMapping>(DamageMappingSerializer)
                register<DamagePredicate>(DamagePredicateSerializer)
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilderSerializer)
            }
        }.buildAndLoadString(
            Injector.get<File>(InjectionQualifier.CONFIGS_FOLDER)
                .resolve(DamageMappingConstants.DATA_DIR)
                .resolve("direct_entity_type_mappings.yml")
                .readText()
        )
        rootNode.node("no_causing").childrenMap()
            .transformKeys<EntityType>()
            .forEach { (entityType, node) ->
                val damageMappingList = node.childrenMap()
                    .map { (_, node) -> node.get<DamageMapping>()?.also { LOGGER.error("Malformed damage mapping at ${node.path()}. Skipped.") } }
                    .filterNotNull()
                damageMappingForNoCause[entityType] = damageMappingList
            }
        rootNode.node("player").childrenMap()
            .transformKeys<EntityType>()
            .forEach { (entityType, node) ->
                val damageMappingList = node.childrenMap()
                    .map { (_, node) -> node.get<DamageMapping>()?.also { LOGGER.error("Malformed damage mapping at ${node.path()}. Skipped.") } }
                    .filterNotNull()
                damageMappingForPlayer[entityType] = damageMappingList
            }
    }
}