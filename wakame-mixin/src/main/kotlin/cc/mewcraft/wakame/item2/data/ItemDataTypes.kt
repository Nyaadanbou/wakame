package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.item2.data.impl.ItemId
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object ItemDataTypes {

    @JvmField
    val ID: ItemDataType<ItemId> = register("id")

    @JvmField
    val LEVEL: ItemDataType<ItemLevel> = register("level")

    @Deprecated("Not implemented")
    internal fun getType(id: String): ItemDataType<*>? {
        TODO("#350: 使用 KoishRegistry")
    }

    @Deprecated("Not implemented")
    internal fun getId(type: ItemDataType<*>): String {
        TODO("#350: 使用 KoishRegistry")
    }

    /**
     * 获取一个 [TypeSerializerCollection]. 返回的实例可用来序列化 [ItemDataContainer] 中的所有数据类型.
     */
    internal fun serializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        // 添加每一个 ItemDataType 的 TypeSerializer
        val dataTypes = listOf(ID, LEVEL) // FIXME #350: 从 KoishRegistry 遍历
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
        // FIXME #350: 在 KoishRegistry 中注册以支持 type dispatching
        return ItemDataType.builder(typeTokenOf<T>()).apply(block).build()
    }

}