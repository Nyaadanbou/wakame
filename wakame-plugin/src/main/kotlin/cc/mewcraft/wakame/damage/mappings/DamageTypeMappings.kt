@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage.mappings

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.serialization.configurate.extension.transformKeys
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.damage.DamageType
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 依据原版伤害类型来获取萌芽伤害的映射.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload
object DamageTypeMappings {

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
    private fun init() = loadDataIntoRegistry()

    @ReloadFun
    private fun reload() = loadDataIntoRegistry()

    private fun loadDataIntoRegistry() {
        mappings.clear()

        val rootNode = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<DamageMetadataBuilder<*>>(DamageMetadataBuilderSerializer)
            }
        }.buildAndLoadString(
            Injector.get<File>(InjectionQualifier.CONFIGS_FOLDER)
                .resolve(DamageMappingConstants.DATA_DIR)
                .resolve("damage_type_mappings.yml").readText()
        )
        for ((damageType, node) in rootNode.childrenMap().transformKeys<DamageType>(throwIfFail = false)) {
            val mapping = node.get<DamageTypeMapping>()
            if (mapping == null) {
                LOGGER.error("Malformed damage type mapping at ${node.path()}. Skipped.")
                continue
            }
            mappings[damageType] = mapping
        }
    }
}

@ConfigSerializable
data class DamageTypeMapping(@Setting("damage_metadata") val builder: DamageMetadataBuilder<*>)
