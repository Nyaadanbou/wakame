package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerRegistryLoader
import cc.mewcraft.wakame.adventure.AudienceMessageGroupSerializer
import cc.mewcraft.wakame.adventure.CombinedAudienceMessageSerializer
import cc.mewcraft.wakame.entity.attribute2.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.*

/**
 * 加载技能类型.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeFacadeRegistryLoader::class, // deps: 需要直接的数据
        AbilityTriggerRegistryLoader::class
    ]
)
@Reload
internal object AbilityMetaRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.ABILITY_META.resetRegistry()

        consumeData(BuiltInRegistries.ABILITY_META::add)
        BuiltInRegistries.ABILITY_META.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(BuiltInRegistries.ABILITY_META::update)
    }

    private fun consumeData(consumer: (Identifier, AbilityMeta) -> Unit) {
        val loader = yamlLoader {
            withDefaults()
            serializers {
                register(AbilityMeta.SERIALIZER)
                register(AudienceMessageGroupSerializer)
                register(CombinedAudienceMessageSerializer)
            }
        }

        // 获取存放所有物品配置的文件夹
        val dataDir = getFileInConfigDirectory("ability/")

        for ((file, _, path) in NamespacedFileTreeWalker(dataDir, fileExtension = "yml", includeFullPath = true, includeNamespacePath = true)) {
            val rootNode = loader.buildAndLoadString(file.readText())
            val abilityId = Identifiers.of(path)
            try {
                consumer(abilityId, rootNode.require<AbilityMeta>())
            } catch (t: Throwable) {
                LOGGER.warn("Failed to load ability: '$abilityId', Path: '${file.path}'", t)
                continue
            }
        }
    }
}