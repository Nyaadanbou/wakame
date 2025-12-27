@file:JvmName("ItemRegistryLoaders")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.item.behavior.ItemBehaviorContainer
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item.property.ItemPropContainer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.loot.LootTableRegistryLoader
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.*
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection


// ------------
// 加载物品注册表
// ------------

private val SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    .register<ItemBehaviorContainer>(ItemBehaviorContainer.makeDirectSerializer())
    .registerAll(ItemPropContainer.makeDirectSerializers())
    .registerAll(ItemMetaContainer.makeDirectSerializers())
    .build()

/**
 * 加载 *自定义物品类型*.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeFacadeRegistryLoader::class, // deps: 需要直接的数据
        LootTableRegistryLoader::class
    ]
)
@Reload(
    runAfter = [
        LootTableRegistryLoader::class // 需要在掉落表加载后加载物品
    ]
)
internal object CustomItemRegistryLoader : RegistryLoader {

    /**
     * 命名空间 `internal` 下的物品仅用于实现内部逻辑.
     */
    private const val INTERNAL_NAMESPACE = "internal"

    @InitFun
    fun init() {
        BuiltInRegistries.ITEM.resetRegistry()
        consumeData(BuiltInRegistries.ITEM::add)
        BuiltInRegistries.ITEM.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(BuiltInRegistries.ITEM::update)
    }

    private fun consumeData(consumer: (Identifier, KoishItem) -> Unit) {
        val loader = yamlLoader {
            withDefaults()
            serializers { registerAll(SERIALIZERS) }
        }

        // 获取存放所有物品配置的文件夹
        val dataDir = getFileInConfigDirectory("item/")

        dataDir.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val itemId = Identifiers.of(f.relativeTo(dataDir).invariantSeparatorsPath.substringBeforeLast('.'))
                val itemValue = loadValue(itemId, rootNode)
                consumer(itemId, itemValue)
            } catch (e: Exception) {
                LOGGER.error("Failed to load custom item config: {}", f.path)
                IdePauser.pauseInIde(e)
            }
        }
    }

    private fun loadValue(id: Identifier, node: ConfigurationNode): KoishItem {
        val dataConfig = node.require<ItemMetaContainer>()
        val properties = node.require<ItemPropContainer>()
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
@Init(stage = InitStage.POST_WORLD) // 套皮物品的初始化需要生成 NMS ItemStack, 而此时会调用一些 Paper 的实例但这些实例并未初始化完毕, 因此 POST_WORLD
@Reload
internal object ItemProxyRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.ITEM_PROXY.resetRegistry()
        consumeData(BuiltInRegistries.ITEM_PROXY::add)
        BuiltInRegistries.ITEM_PROXY.freeze()

        // 检查配置文件是否都是有效的原版物品, 并给出警告
        runTask(::validateItemProxies)
    }

    @ReloadFun
    fun reload() {
        consumeData(BuiltInRegistries.ITEM_PROXY::update)
    }

    private fun consumeData(consumer: (Identifier, KoishItemProxy) -> Unit) {
        val loader = yamlLoader {
            withDefaults()
            serializers { registerAll(SERIALIZERS) }
        }

        // 获取存放所有物品配置的文件夹
        val dataDir = getFileInConfigDirectory("item_proxied/")

        for (f in dataDir.walk().drop(1).filter { it.isFile && it.extension == "yml" }) {
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val itemId = Identifiers.of(Identifier.MINECRAFT_NAMESPACE, f.relativeTo(dataDir).invariantSeparatorsPath.substringBeforeLast('.'))
                val itemValue = loadValue(itemId, rootNode)
                consumer(itemId, itemValue)
            } catch (e: Exception) {
                LOGGER.error("Failed to load item proxy config: {}", f.path)
                IdePauser.pauseInIde(e)
            }
        }
    }

    private fun loadValue(id: Identifier, node: ConfigurationNode): KoishItemProxy {
        val dataConfig = node.require<ItemMetaContainer>()
        val properties = node.require<ItemPropContainer>()
        val behaviors = node.require<ItemBehaviorContainer>()
        val koishItem = KoishItem(id, dataConfig, properties, behaviors)

        // 生成一个完整的 ItemStack, 但只取其 ItemDataContainer
        val tempItemstack = KoishStackGenerator.generate(koishItem, ItemGenerationContext(koishItem, 0f, 0))
        val dataContainer = tempItemstack.dataContainer(false) ?: error("The generated ItemStack has no ItemDataContainer. This is a bug!")

        return KoishItemProxy(id, dataConfig, properties, behaviors, dataContainer)
    }

    private fun validateItemProxies() {
        for (itemType in BuiltInRegistries.ITEM_PROXY) {
            if (!isMinecraftItem(itemType.id)) {
                LOGGER.error("Found a non-Minecraft proxy item config: ${itemType.id}. The config will be effectively ignored.")
            }
        }
    }

    private fun isMinecraftItem(id: Identifier): Boolean {
        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)
        return registry.get(id) != null
    }
}