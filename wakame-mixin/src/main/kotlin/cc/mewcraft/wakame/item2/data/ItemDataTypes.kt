package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.item2.data.impl.ItemId
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object ItemDataTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val ID: ItemDataType<ItemId> = register("id")

    @JvmField
    val VERSION: ItemDataType<Int> = register("version")

    @JvmField
    val NETWORK_REWRITE: ItemDataType<Boolean> = register("network_rewrite")

    @JvmField
    val PROCESSED: ItemDataType<Boolean> = register("processed")

    @JvmField
    val LEVEL: ItemDataType<ItemLevel> = register("level")

    // ------------
    // 方便函数
    // ------------

    /**
     * 获取一个 [TypeSerializerCollection]. 返回的实例可用来序列化 [ItemDataContainer] 中的所有数据类型.
     */
    internal fun serializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        // 添加每一个 ItemDataType<T> 的 TypeSerializer<T>
        val dataTypes = KoishRegistries2.ITEM_DATA_TYPE.valueSequence
        for (dataType in dataTypes) {
            val serializers = dataType.serializers
            if (serializers != null) {
                collection.registerAll(serializers)
            }
        }

        return collection.build()
    }

    /**
     * @param id 将作为 Registry 中的 id
     * @param block 用于配置 [ItemDataType]
     */
    private inline fun <reified T> register(id: String, block: ItemDataType.Builder<T>.() -> Unit = {}): ItemDataType<T> {
        val type = ItemDataType.builder(typeTokenOf<T>()).apply(block).build()
        KoishRegistries2.ITEM_DATA_TYPE.add(id, type)
        return type
    }

}