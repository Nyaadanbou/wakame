package cc.mewcraft.wakame.entity.attribute.source

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.configurate.yamlLoader
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.kotlin.extensions.getList
import java.io.File

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        AttributeFacadeRegistryLoader::class, // deps: 需要直接的数据, 必须在其之后
    ]
)
internal object AttributeSourceRegistry {
    /**
     * [AttributeSource] 配置文件位置.
     */
    private val CONFIG_FILE: File = KoishDataPaths.CONFIGS.resolve("damage").resolve("config.yml").toFile()

    // 未来可能还有其他的属性来源
    private val effectType2AttributeSources: Reference2ObjectMap<PotionEffectType, List<EffectAttributeSource>> = Reference2ObjectOpenHashMap()

    /**
     * 获取 [EffectAttributeSource].
     */
    fun byEffectType(effectType: PotionEffectType) = effectType2AttributeSources[effectType]

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    fun reload() {
        loadDataIntoRegistry()
    }

    fun loadDataIntoRegistry() {
        effectType2AttributeSources.clear()

        val rootNode = yamlLoader {
            withDefaults()
            serializers {
                register<EffectAttributeSource>(EffectAttributeSource.serializer())
                registerExact<NumberModifier>(NumberModifier.serializer())
            }
        }.buildAndLoadString(CONFIG_FILE.readText())
        val sourceNode = rootNode.node("attribute_modifier_source")

        val effectNode = sourceNode.node("effect")
        for ((nodeKey, childNode) in effectNode.childrenMap()) {
            val key = Key.key(nodeKey.toString())
            val potionEffectType = Registry.MOB_EFFECT.get(key) ?: run {
                LOGGER.warn("Unknown potion effect type: '$nodeKey', skipping")
                continue
            }

            childNode.hint(RepresentationHints.EFFECT_TYPE, potionEffectType)
            val effectAttributeSources = childNode.getList<EffectAttributeSource>(emptyList())
            effectType2AttributeSources[potionEffectType] = effectAttributeSources
        }

        LOGGER.info("Loaded ${effectType2AttributeSources.size} effect attribute sources.")
    }
}