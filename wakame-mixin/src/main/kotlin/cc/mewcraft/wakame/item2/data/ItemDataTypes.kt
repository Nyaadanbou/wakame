package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item2.data.impl.*
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.serializer.JsonComponentSerializer
import cc.mewcraft.wakame.serialization.configurate.serializer.holderByNameTypeSerializer
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 该 `object` 包含了所有可用的 [ItemDataType].
 *
 * [ItemDataType] 的数据将存在于*物品堆叠*上, 而不是*物品类型*上.
 * 也就是说, 这些数据将会被持久化, 并且可以在游戏内被(玩家或系统)修改.
 *
 * @see cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes 用于控制如何生成物品堆叠上的持久化数据类型
 * @see cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes 包含了所有存在于*物品类型*上的数据类型
 */
data object ItemDataTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val ID: ItemDataType<ItemId> = typeOf("id") {
        serializers {
            register<ItemId>(ItemId.SERIALIZER)
        }
    }

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

    @JvmField
    val RARITY: ItemDataType<RegistryEntry<Rarity>> = typeOf("rarity") {
        serializers {
            register(BuiltInRegistries.RARITY.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val KIZAMI: ItemDataType<Set<RegistryEntry<Kizami>>> = typeOf("kizami") {
        serializers {
            register(BuiltInRegistries.KIZAMI.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val ELEMENT: ItemDataType<Set<RegistryEntry<Element>>> = typeOf("element") {
        serializers {
            register(BuiltInRegistries.ELEMENT.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val CORE: ItemDataType<Core> = typeOf("core") {
        serializers {
            registerAll(Core.serializers())
        }
    }

    @JvmField
    val CORE_CONTAINER: ItemDataType<CoreContainer> = typeOf("cores") {
        serializers {
            register(CoreContainer.SERIALIZER)
            registerAll(Core.serializers())
        }
    }

    @JvmField
    val CRATE: ItemDataType<ItemCrate> = typeOf("crate")

    @JvmField
    val REFORGE_HISTORY: ItemDataType<ReforgeHistory> = typeOf("reforge_history")

    /**
     * 桶中生物的信息, 会经过渲染后展示给玩家看.
     */
    @JvmField
    val ENTITY_BUCKET_INFO: ItemDataType<EntityBucketInfo> = typeOf("entity_bucket_info") {
        serializers {
            registerAll(EntityBucketInfo.serializers())
        }
    }

    /**
     * 桶中生物的 NBT 数据.
     */
    @JvmField
    val ENTITY_BUCKET_DATA: ItemDataType<ByteArray> = typeOf("entity_bucket_data")

    /**
     * 旧的 `minecraft:item_name` 信息, 用于在某些操作后将物品的 `minecraft:item_name` 恢复为原本的值.
     */
    @JvmField
    val PREVIOUS_ITEM_NAME: ItemDataType<Component> = typeOf("previous_item_name") {
        serializers {
            register(JsonComponentSerializer)
        }
    }

    /**
     * 额外附加在物品上的附魔槽位数量.
     */
    @JvmField
    val EXTRA_ENCHANT_SLOTS: ItemDataType<Int> = typeOf("extra_enchant_slots")

    /**
     * 记录了一个酒酿配方.
     *
     * 需要安装对应插件才能正常使用.
     */
    @JvmField
    val BREW_RECIPE: ItemDataType<ItemBrewRecipe> = typeOf("brew_recipe")

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