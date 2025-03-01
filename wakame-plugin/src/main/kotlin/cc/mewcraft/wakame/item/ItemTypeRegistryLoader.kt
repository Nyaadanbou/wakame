package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 加载物品注册表的逻辑.
 *
 * 本单例具体负责两个注册表:
 * 1. 一般萌芽物品的注册表
 * 2. 原版套皮物品的注册表
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AbilityRegistry::class,
    ]
)
@Reload(
    runAfter = [
        AbilityRegistry::class,
    ]
)
object ItemTypeRegistryLoader : RegistryConfigStorage {

    /**
     * 命名空间 `minecraft` 下的物品仅用于实现原版套皮物品,
     * 在游戏内不允许通过指令/后台指令, 图鉴等手段获取.
     * 代码上仍然可以直接访问该命名空间下的物品.
     */
    private const val MINECRAFT_NAMESPACE = "minecraft"

    /**
     * 命名空间 `internal` 下的物品仅用于实现内部逻辑.
     */
    private const val INTERNAL_NAMESPACE = "internal"

    /**
     * 物品的序列化器集合.
     */
    @JvmField
    internal val SERIALIZERS = TypeSerializerCollection.builder()
        .register(ItemBaseSerializer)
        .register(ItemSlotSerializer)
        .register(ItemSlotGroupSerializer)
        .registerAll(ItemTemplateTypes.serializers()) // 每个模板的序列化器
        .build()

    @InitFun
    fun init() {
        KoishRegistries.ITEM.resetRegistry()
        applyDataToRegistry(KoishRegistries.ITEM::add)
        KoishRegistries.ITEM.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.ITEM::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, NekoItem) -> Unit) {
        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                registerAll(SERIALIZERS)
            }
        }

        for ((file, namespace, path) in NamespacedFileTreeWalker(getFileInConfigDirectory("item/"), fileExtension = "yml", includeFullPath = true)) {
            val rootNode = loader.buildAndLoadString(file.readText())
            val id = Identifier.key(namespace, path)
            try {
                val item = if (namespace == MINECRAFT_NAMESPACE) {
                    NekoItemFactory.VANILLA(id, rootNode)
                } else {
                    NekoItemFactory.STANDARD(id, rootNode)
                }
                registryAction(id, item)
            } catch (e: Exception) {
                LOGGER.error("An error occurred while loading file: {}", file.path)
                Util.pauseInIde(e)
            }
        }
    }
}