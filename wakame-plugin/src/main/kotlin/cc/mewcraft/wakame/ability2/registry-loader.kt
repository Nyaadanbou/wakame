package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.meta.AbilityMetaContainer
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerRegistryLoader
import cc.mewcraft.wakame.adventure.AudienceMessageGroupSerializer
import cc.mewcraft.wakame.adventure.CombinedAudienceMessageSerializer
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

private val SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    .registerAll(AbilityMetaContainer.makeSerializers())
    .register(AudienceMessageGroupSerializer)
    .register(CombinedAudienceMessageSerializer)
    .build()

/**
 * 加载技能类型.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeBundleFacadeRegistryLoader::class, // deps: 需要直接的数据
        AbilityTriggerRegistryLoader::class
    ]
)
@Reload
internal object AbilityMetaRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        KoishRegistries2.ABILITY_META.resetRegistry()

        consumeData(KoishRegistries2.ABILITY_META::add)
        KoishRegistries2.ABILITY_META.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(KoishRegistries2.ABILITY_META::update)
    }

    private fun consumeData(consumer: (Identifier, AbilityMeta) -> Unit) {
        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers { registerAll(SERIALIZERS) }
        }

        // 获取存放所有物品配置的文件夹
        val dataDir = getFileInConfigDirectory("ability/")

        for ((file, _, path) in NamespacedFileTreeWalker(dataDir, fileExtension = "yml", includeFullPath = true, includeNamespacePath = true)) {
            val rootNode = loader.buildAndLoadString(file.readText())
            val abilityId = Identifiers.of(path)
            try {
                consumer(abilityId, loadValue(rootNode))
            } catch (t: Throwable) {
                LOGGER.warn("Failed to load ability: '$abilityId', Path: '${file.path}'", t)
                continue
            }
        }
    }

    private fun loadValue(node: ConfigurationNode): AbilityMeta {
        val dataConfig = node.require<AbilityMetaContainer>()
        return AbilityMeta(dataConfig)
    }
}