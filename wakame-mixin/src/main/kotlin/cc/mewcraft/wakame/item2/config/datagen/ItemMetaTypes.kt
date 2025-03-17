package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemLevel
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.KOISH_CONFIGURATE_SERIALIZERS_2
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 该 object 包含了所有可能的 [ItemMetaType].
 *
 * 如果程序员需要给一个可以持久化的物品数据提供配置文件, 那就在这里注册一个新类型.
 * 如果一个可以持久化的物品数据不需要配置文件 (例如: 统计数据), 则不需要考虑此系统.
 */
data object ItemMetaTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val LEVEL: ItemMetaType<MetaItemLevel, ItemLevel> = typeOf("level") {
        // 开发日记: 这里指定的 TypeSerializer 是对于 U 而不是 V
        serializers {
            register(MetaItemLevel.SERIALIZER)
        }
    }

    // 开发日记: 数据类型 V 不一定需要封装类 (如 ItemLevel), 只需要可以被序列化.
    //  这里直接使用了 Component 作为 V, 没有必要再去创建一个新的类型来封装它.
    @JvmField
    val ITEM_NAME: ItemMetaType<MetaItemName, Component> = typeOf("item_name")

    /**
     * 获取一个 [TypeSerializerCollection] 实例, 可用来序列化 [ItemMetaContainer] 中的数据类型.
     *
     * 该 [TypeSerializerCollection] 的序列化代码被调用的时机发生在 *加载物品配置文件* 时.
     */
    internal fun serializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        KoishRegistries2.ITEM_META_TYPE.fold(collection) { acc, type ->
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
     * @param block 用于配置 [ItemMetaType]
     */
    private inline fun <reified U, V> typeOf(id: String, block: ItemMetaType.Builder<U, V>.() -> Unit = {}): ItemMetaType<U, V> {
        val type = ItemMetaType.builder<U, V>(typeTokenOf<U>()).apply(block).build()
        return type.also { KoishRegistries2.ITEM_META_TYPE.add(id, it) }
    }

}