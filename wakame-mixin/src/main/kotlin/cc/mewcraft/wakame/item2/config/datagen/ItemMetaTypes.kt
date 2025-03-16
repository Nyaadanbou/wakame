package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemLevel
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data object ItemMetaTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val LEVEL: ItemMetaType<MetaItemLevel> = typeOf("level") {
        serializers { register(MetaItemLevel.SERIALIZER) }
        // + Some other configurations ...
    }

    /**
     * 获取一个 [TypeSerializerCollection] 实例, 可用来序列化 [ItemMetaContainer] 中的数据类型.
     *
     * 该 [TypeSerializerCollection] 实例被调用的时机发生在 *加载物品配置文件* 时.
     */
    internal fun serializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        KoishRegistries2.ITEM_META_TYPE.valueSequence.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemMetaType]
     */
    private inline fun <reified T> typeOf(id: String, block: ItemMetaType.Builder<T>.() -> Unit = {}): ItemMetaType<T> {
        val type = ItemMetaType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { KoishRegistries2.ITEM_META_TYPE.add(id, it) }
    }

}