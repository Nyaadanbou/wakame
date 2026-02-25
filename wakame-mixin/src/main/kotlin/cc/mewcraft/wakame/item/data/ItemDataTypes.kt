package cc.mewcraft.wakame.item.data

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.data.impl.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.mixin.support.ItemKey
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.serializer.JsonComponentSerializer
import cc.mewcraft.wakame.serialization.configurate.serializer.holderByNameTypeSerializer
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 该 `object` 包含了所有可用的 [ItemDataType].
 *
 * [ItemDataType] 的数据将存在于*物品堆叠*上, 而不是*物品类型*上.
 * 也就是说, 这些数据将会被持久化, 并且可以在游戏内被(玩家或系统)修改.
 *
 * @see cc.mewcraft.wakame.item.datagen.ItemMetaTypes 用于控制如何生成物品堆叠上的持久化数据类型
 * @see cc.mewcraft.wakame.item.property.ItemPropTypes 包含了所有存在于*物品类型*上的数据类型
 */
data object ItemDataTypes {

    // ------------
    // 注册表
    // ------------

    @Deprecated("实现上不再使用")
    @JvmField
    val ID: ItemDataType<ItemKey> = typeOf("id") {
        persistent(true)
        serializers {
            register<ItemKey>(ItemKey.serializer())
        }
    }

    @Deprecated("策划上从未使用")
    @JvmField
    val VARIANT: ItemDataType<Int> = typeOf("variant") {
        persistent(true)
    }

    @JvmField
    val VERSION: ItemDataType<Int> = typeOf("version") {
        persistent(true)
    }

    @JvmField
    val BYPASS_NETWORK_REWRITE: ItemDataType<Unit> = typeOf("bypass_network_rewrite")

    @JvmField
    val ONLY_COMPARE_ID_IN_RECIPE_BOOK: ItemDataType<Unit> = typeOf("only_compare_id_in_recipe")

    @JvmField
    val LEVEL: ItemDataType<ItemLevel> = typeOf("level") {
        persistent(true)
    }

    @JvmField
    val RARITY: ItemDataType<RegistryEntry<Rarity>> = typeOf("rarity") {
        persistent(true)
        serializers {
            register(BuiltInRegistries.RARITY.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val KIZAMI: ItemDataType<Set<RegistryEntry<Kizami>>> = typeOf("kizami") {
        persistent(true)
        serializers {
            register(BuiltInRegistries.KIZAMI.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val ELEMENT: ItemDataType<Set<RegistryEntry<Element>>> = typeOf("element") {
        persistent(true)
        serializers {
            register(BuiltInRegistries.ELEMENT.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val CORE: ItemDataType<Core> = typeOf("core") {
        persistent(true)
        serializers {
            registerAll(Core.serializers())
        }
    }

    @JvmField
    val CORE_CONTAINER: ItemDataType<CoreContainer> = typeOf("cores") {
        persistent(true)
        serializers {
            register(CoreContainer.SERIALIZER)
            registerAll(Core.serializers())
        }
    }

    @JvmField
    val CRATE: ItemDataType<ItemCrate> = typeOf("crate") {
        persistent(true)
    }

    @JvmField
    val REFORGE_HISTORY: ItemDataType<ReforgeHistory> = typeOf("reforge_history") {
        persistent(true)
    }

    /**
     * 桶中生物的信息, 会经过渲染后展示给玩家看.
     */
    @JvmField
    val ENTITY_BUCKET_INFO: ItemDataType<EntityBucketInfo> = typeOf("entity_bucket_info") {
        persistent(true)
        serializers {
            registerAll(EntityBucketInfo.serializers())
        }
    }

    /**
     * 桶中生物的 NBT 数据.
     */
    @JvmField
    val ENTITY_BUCKET_DATA: ItemDataType<ByteArray> = typeOf("entity_bucket_data") {
        persistent(true)
    }

    /**
     * 旧的 `minecraft:item_name` 信息, 用于在某些操作后将物品的 `minecraft:item_name` 恢复为原本的值.
     */
    @JvmField
    val PREVIOUS_ITEM_NAME: ItemDataType<Component> = typeOf("previous_item_name") {
        persistent(true)
        serializers {
            register(JsonComponentSerializer)
        }
    }

    /**
     * 额外附加在物品上的附魔槽位数量.
     */
    @JvmField
    val EXTRA_ENCHANT_SLOTS: ItemDataType<Int> = typeOf("extra_enchant_slots") {
        persistent(true)
    }

    /**
     * 记录了一个酒酿配方.
     *
     * 需要安装对应插件才能正常使用.
     */
    @JvmField
    val BREW_RECIPE: ItemDataType<ItemBrewRecipe> = typeOf("brew_recipe") {
        persistent(true)
    }

    /**
     * 记录了一个网络中的坐标.
     */
    @JvmField
    val NETWORK_POSITION: ItemDataType<NetworkPosition> = typeOf("network_position") {
        persistent(true)
    }

    /**
     * 标记盲盒钥匙已经被替换.
     */
    @JvmField
    val CRATE_KEY_REPLACED: ItemDataType<Unit> = typeOf("crate_key_replaced") {
        persistent(true)
    }

    /**
     * 该数据仅存在于网络物品上!
     *
     * 物品堆叠所在的物品槽位.
     * 具体的物品栏位置参考:
     *
     * Converted Slots:
     * ```
     * 39             1  2     0
     * 38             3  4
     * 37
     * 36          40
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 0  1  2  3  4  5  6  7  8
     * ```
     */
    @JvmField
    val SLOT: ItemDataType<Int> = typeOf("slot")

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemDataType]
     */
    private inline fun <reified T> typeOf(id: String, block: ItemDataType.Builder<T>.() -> Unit = {}): ItemDataType<T> {
        val type = ItemDataType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { BuiltInRegistries.ITEM_DATA_TYPE.add(id, it) }
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
        BuiltInRegistries.ITEM_DATA_TYPE.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }

}