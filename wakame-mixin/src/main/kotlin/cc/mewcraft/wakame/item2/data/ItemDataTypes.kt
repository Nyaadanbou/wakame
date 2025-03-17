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
    val VERSION: ItemDataType<Int> = typeOf("ver")

    // FIXME #350: 似乎有个问题:
    //  如果直接在 StreamCodec 中将自定义数据抹除, 这个数据也将无法在发包时读取到.
    //  如果发包系统想读取这个数据, 那么是读取不到的. 也就是说, 这些数据需要另外的系统去存放.
    //  ---
    //  还是说, 数据是先构建为 Packet 实例, 最后在发送时才会使用 StreamCodec 将数据包转换为 ByteBuf?
    @JvmField
    val BYPASS_NETWORK_REWRITE: ItemDataType<Unit> = typeOf("bypass_network_rewrite")

    // FIXME #350: 同上
    @JvmField
    val PROCESSED: ItemDataType<Unit> = typeOf("processed")

    @JvmField
    val LEVEL: ItemDataType<ItemLevel> = typeOf("level")

    /**
     * 获取一个 [TypeSerializerCollection], 可用来序列化 [ItemDataContainer] 中的数据类型.
     *
     * 该 [TypeSerializerCollection] 的序列化代码调用的时机与 ItemStack 发生序列化的时机一致, 即:
     * - 服务端从持久化媒介读取 ItemStack 的数据时, 例如 读取区块和实体, 加载玩家物品栏
     * - 服务端往持久化媒介保存 ItemStack 的数据时, 例如 执行保存任务
     *
     * 例外: 这些序列化代码不会参与发包, 因为发包时不会发送任何自定义数据 (hint: Mixin).
     */
    internal fun serializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        // 添加每一个 ItemDataType<T> 的 TypeSerializer<T>
        KoishRegistries2.ITEM_DATA_TYPE.fold(collection) { acc, type ->
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