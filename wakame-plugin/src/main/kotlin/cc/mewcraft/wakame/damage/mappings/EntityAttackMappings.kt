@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.DamageMetadataBuilder
import cc.mewcraft.wakame.damage.DamageMetadataBuilderSerializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.serialization.configurate.extension.transformKeys
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.File

/**
 * 依据原版生物的攻击特征来获取萌芽伤害的映射.
 */
@Init(
    stage = InitStage.POST_WORLD,
)
@Reload
object EntityAttackMappings {

    private val mappings: Reference2ObjectOpenHashMap<EntityType, List<DamageMapping>> = Reference2ObjectOpenHashMap()

    @InitFun
    private fun init() = loadDataIntoRegistry()

    @ReloadFun
    private fun reload() = loadDataIntoRegistry()

    /**
     * 获取某一伤害情景下原版生物的伤害映射.
     * 返回空表示未指定该情景下的伤害映射.
     */
    fun getForVanilla(damager: LivingEntity, event: EntityDamageEvent): DamageMapping? {
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
            Injector.get<File>(InjectionQualifier.CONFIGS_FOLDER)
                .resolve(DamageMappingConstants.DATA_DIR)
                .resolve("entity_attack_mappings.yml")
                .readText()
        )
        for ((entityType, node) in rootNode.childrenMap().transformKeys<EntityType>()) {
            val damageMappingList = node.childrenMap().map { (_, node) ->
                node.get<DamageMapping>()?.also { LOGGER.error("Malformed damage mapping at ${node.path()}. Skipped.") }
            }.filterNotNull()
            mappings[entityType] = damageMappingList
        }
    }
}