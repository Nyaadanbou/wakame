package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.item2.config.property.impl.Arrow
import cc.mewcraft.wakame.item2.config.property.impl.ItemBase2
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot2
import cc.mewcraft.wakame.item2.config.property.impl.Lore
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.KeySerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data object ItemPropertyTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val ID: ItemPropertyType<Identifier> = typeOf("id") {
        serializers { register(KeySerializer) }
    }

    @JvmField
    val BASE: ItemPropertyType<ItemBase2> = typeOf("base")

    @JvmField
    val SLOT: ItemPropertyType<ItemSlot2> = typeOf("slot")

    @JvmField
    val HIDDEN: ItemPropertyType<Unit> = typeOf("hidden")

    @JvmField
    val ARROW: ItemPropertyType<Arrow> = typeOf("arrow")

    @JvmField
    val CASTABLE: ItemPropertyType<Unit> = typeOf("castable")

    @JvmField
    val GLOWABLE: ItemPropertyType<Unit> = typeOf("glowable")

    @JvmField
    val LORE: ItemPropertyType<Lore> = typeOf("lore")

    /**
     * 获取一个 [TypeSerializerCollection] 实例, 可用来序列化 [ItemPropertyContainer] 中的数据类型.
     *
     * 该 [TypeSerializerCollection] 实例被调用的时机发生在 *加载物品配置文件* 时.
     */
    internal fun serializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        KoishRegistries2.ITEM_PROPERTY_TYPE.valueSequence.fold(collection) { acc, type ->
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
     * @param block 用于配置 [ItemPropertyType]
     */
    private inline fun <reified T> typeOf(id: String, block: ItemPropertyType.Builder<T>.() -> Unit = {}): ItemPropertyType<T> {
        val type = ItemPropertyType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { KoishRegistries2.ITEM_PROPERTY_TYPE.add(id, it) }
    }

}