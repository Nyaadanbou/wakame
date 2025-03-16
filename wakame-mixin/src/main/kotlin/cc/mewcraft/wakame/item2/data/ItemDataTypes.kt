package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.item2.data.impl.ItemId
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.KOISH_CONFIGURATE_SERIALIZERS_2
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data object ItemDataTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val ID: ItemDataType<ItemId> = typeOf("id")

    @JvmField
    val VERSION: ItemDataType<Int> = typeOf("version")

    @JvmField
    val BYPASS_NETWORK_REWRITE: ItemDataType<Unit> = typeOf("bypass_network_rewrite")

    @JvmField
    val PROCESSED: ItemDataType<Unit> = typeOf("processed")

    @JvmField
    val LEVEL: ItemDataType<ItemLevel> = typeOf("level")

    /**
     * 获取一个 [TypeSerializerCollection], 可用来序列化 [ItemDataContainer] 中的数据类型.
     *
     * 该 [TypeSerializerCollection] 被调用的时机与 ItemStack 发生序列化的时机一致, 即:
     * - 读取 ItemStack 的数据时, 例如 服务端读取地图, 加载玩家物品栏
     * - 保存 ItemStack 的数据时, 例如 服务端执行保存任务
     */
    internal fun serializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        // 添加每一个 ItemDataType<T> 的 TypeSerializer<T>
        KoishRegistries2.ITEM_DATA_TYPE.valueSequence.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        collection.registerAll(KOISH_CONFIGURATE_SERIALIZERS_2)

        return collection.build()
    }

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemDataType]
     */
    private inline fun <reified T> typeOf(id: String, block: ItemDataType.Builder<T>.() -> Unit = {}): ItemDataType<T> {
        val type = ItemDataType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { KoishRegistries2.ITEM_DATA_TYPE.add(id, it) }
    }

}