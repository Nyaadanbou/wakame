package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.item2.data.impl.ItemId
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 该 `object` 包含了所有可用的 [ItemDataType].
 *
 * [ItemDataType] 的数据将存在于*物品堆叠*上, 而不是*物品类型*上.
 * 也就是说, 这些数据将会被持久化, 并且可以在游戏内被(玩家或系统)修改.
 *
 * @see cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes 用于控制如何生成物品堆叠上的持久化数据
 * @see cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes 包含了所有存在于*物品类型*上的数据
 */
data object ItemDataTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val ID: ItemDataType<ItemId> = typeOf("id")

    @JvmField
    val VARIANT: ItemDataType<Int> = typeOf("variant")

    @JvmField
    val VERSION: ItemDataType<Int> = typeOf("version")

    @JvmField
    val BYPASS_NETWORK_REWRITE: ItemDataType<Unit> = typeOf("bypass_network_rewrite")

    @JvmField
    val PROCESSED: ItemDataType<Unit> = typeOf("processed")

    @JvmField
    val LEVEL: ItemDataType<ItemLevel> = typeOf("level")

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

    // ------------
    // 内部实现
    // ------------

    /**
     * 获取一个 [TypeSerializerCollection], 可用来序列化 [ItemDataContainer] 中的数据类型.
     *
     * 返回的 [TypeSerializerCollection] 仅包含在这里显式声明的序列化操作, 不包含隐式声明的例如 [Int].
     *
     * @see ItemDataContainer.makeCodec
     */
    internal fun directSerializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        // 添加每一个 “Item Data” 的 TypeSerializer
        KoishRegistries2.ITEM_DATA_TYPE.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }

}