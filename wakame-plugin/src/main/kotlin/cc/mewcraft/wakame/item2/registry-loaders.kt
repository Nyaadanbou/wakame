package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.trigger.TriggerRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryLoader
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContainer
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item2.config.property.ItemPropertyContainer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.*
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

// FIXME #350: 添加序列化器
private val SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    .register(ItemMetaContainer.makeSerializer())
    .register(ItemBehaviorContainer.makeSerializer())
    .register(ItemPropertyContainer.makeSerializer())
    .build()

/**
 * 加载 *自定义物品类型*.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeBundleFacadeRegistryLoader::class, // deps: 需要直接的数据
        TriggerRegistryLoader::class
    ]
)
@Reload
internal object CustomItemRegistryLoader : RegistryConfigStorage {

    /**
     * 命名空间 `internal` 下的物品仅用于实现内部逻辑.
     */
    private const val INTERNAL_NAMESPACE = "internal"

    @InitFun
    fun init() {
        KoishRegistries2.ITEM.resetRegistry()
        consumeData(KoishRegistries2.ITEM::add)
        KoishRegistries2.ITEM.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(KoishRegistries2.ITEM::update)
    }

    private fun consumeData(consumer: (Identifier, KoishItem) -> Unit) {
        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers { registerAll(SERIALIZERS) }
        }

        // 获取存放所有物品配置的文件夹
        val dataDir = getFileInConfigDirectory("item2/")

        dataDir.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            val rootNode = loader.buildAndLoadString(f.readText())
            val itemId = IdentifierTools.of(f.toRelativeString(dataDir).substringBeforeLast('.'))
            val itemValue = loadValue(itemId, rootNode)
            consumer(itemId, itemValue)
        }
    }

    private fun loadValue(id: Identifier, node: ConfigurationNode): KoishItem {
        val dataConfig = node.require<ItemMetaContainer>()
        val properties = node.require<ItemPropertyContainer>()
        val behaviors = node.require<ItemBehaviorContainer>()
        return KoishItem(id, dataConfig, properties, behaviors)
    }
}


/**
 * 加载 *原版套皮物品类型*.
 *
 * 命名空间 `minecraft` 下的物品仅用于实现原版套皮物品,
 * 在游戏内不允许通过指令/后台指令, 图鉴等手段获取.
 * 代码上仍然可以直接访问该命名空间下的物品.
 */
@Init(stage = InitStage.PRE_WORLD, runAfter = [CustomItemRegistryLoader::class])
@Reload
internal object ItemProxyRegistryLoader : RegistryConfigStorage {

    @InitFun
    fun init() {
        KoishRegistries2.ITEM_PROXY.resetRegistry()
        consumeData(KoishRegistries2.ITEM_PROXY::add)
        KoishRegistries2.ITEM_PROXY.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(KoishRegistries2.ITEM_PROXY::update)
    }

    private fun consumeData(consumer: (Identifier, KoishItemProxy) -> Unit) {
        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers { registerAll(SERIALIZERS) }
        }

        // 获取存放所有物品配置的文件夹
        val dataDir = getFileInConfigDirectory("item2_proxied/")

        for (f in dataDir.walk().drop(1).filter { it.isFile && it.extension == "yml" }) {
            val rootNode = loader.buildAndLoadString(f.readText())
            val itemId = IdentifierTools.of(Identifier.MINECRAFT_NAMESPACE, f.toRelativeString(dataDir).substringBeforeLast('.'))
            if (!isMinecraftItem(itemId)) {
                LOGGER.error("Found a non-Minecraft item config in ${dataDir.name}: ${f.name}. Skipped.")
                continue
            }
            val itemValue = loadValue(itemId, rootNode)
            consumer(itemId, itemValue)
        }
    }

    private fun loadValue(id: Identifier, node: ConfigurationNode): KoishItemProxy {
        val dataConfig = node.require<ItemMetaContainer>()
        val properties = node.require<ItemPropertyContainer>()
        val behaviors = node.require<ItemBehaviorContainer>()
        val koishItem = KoishItem(id, dataConfig, properties, behaviors)
        TODO("#350: 生成 ItemData")
    }

    private fun isMinecraftItem(id: Identifier): Boolean {
        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)
        return registry.get(id) != null
    }
}