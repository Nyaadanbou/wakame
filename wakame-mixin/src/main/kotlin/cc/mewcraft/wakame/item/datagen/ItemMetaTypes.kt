package cc.mewcraft.wakame.item.datagen

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.data.impl.CoreContainer
import cc.mewcraft.wakame.item.data.impl.ItemLevel
import cc.mewcraft.wakame.item.datagen.impl.*
import cc.mewcraft.wakame.item.property.ItemPropContainer
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.serializer.holderByNameTypeSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import io.papermc.paper.datacomponent.item.TooltipDisplay
import io.papermc.paper.datacomponent.item.UseCooldown
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 该 `object` 包含了所有可用的 [ItemMetaType].
 *
 * 如果程序员需要给一个可以持久化的物品数据提供配置文件, 那就在这里注册一个新类型.
 * 如果一个可以持久化的物品数据不需要配置文件 (例如: 统计数据), 则不需要考虑此系统.
 *
 * @see cc.mewcraft.wakame.item.data.ItemDataTypes
 */
data object ItemMetaTypes {

    // ------------
    // 注册表
    // ------------

    // !!! 注册类型 注意事项 !!!
    //
    // 这里注册的数据类型是讲究顺序的.
    // 在生成物品数据时: 越靠前注册的数据类型, 会越早生成然后写入物品堆叠.
    // 也就是说, 如果数据 X 的生成依赖数据 Y, 则应该把 X 写在 Y 的后面.

    @JvmField
    val LEVEL: ItemMetaType<MetaItemLevel, ItemLevel> = typeOf("level") {
        // 开发日记: 这里指定的 TypeSerializer 是对于 U 而不是 V
        serializers {
            register(MetaItemLevel.SERIALIZER)
        }
    }

    @JvmField
    val RARITY: ItemMetaType<MetaRarity, RegistryEntry<Rarity>> = typeOf("rarity") {
        serializers {
            registerExact(MetaRarity.SERIALIZER)
            register(BuiltInRegistries.RARITY.holderByNameTypeSerializer())
            register(BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.holderByNameTypeSerializer())
        }
    }

    // 开发日记: 数据类型 V 不一定需要封装类 (如 ItemLevel), 只需要可以被序列化.
    //  这里直接使用了 Component 作为 V, 没有必要再去创建一个新的类型来封装它.
    @JvmField
    val ITEM_NAME: ItemMetaType<MetaItemName, Component> = typeOf("item_name")

    @JvmField
    val CUSTOM_NAME: ItemMetaType<MetaCustomName, Component> = typeOf("custom_name")

    @JvmField
    val TOOLTIP_DISPLAY: ItemMetaType<MetaTooltipDisplay, TooltipDisplay> = typeOf("tooltip_display")

    @JvmField
    val ITEM_MODEL: ItemMetaType<MetaItemModel, Key> = typeOf("item_model") {
        serializers {
            registerExact(MetaItemModel.SERIALIZER)
        }
    }

    @JvmField
    val KIZAMI: ItemMetaType<MetaKizami, Set<RegistryEntry<Kizami>>> = typeOf("kizami") {
        serializers {
            registerExact(MetaKizami.SERIALIZER)
            register(BuiltInRegistries.KIZAMI.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val ELEMENT: ItemMetaType<MetaElement, Set<RegistryEntry<Element>>> = typeOf("element") {
        serializers {
            register(BuiltInRegistries.ELEMENT.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val CORE_CONTAINER: ItemMetaType<MetaCoreContainer, CoreContainer> = typeOf("cores") {
        serializers {
            registerExact(MetaCoreContainer.SERIALIZER)
            register(CoreContainer.SERIALIZER)
            registerAll(Core.serializers())
        }
    }

    @JvmField
    val USE_COOLDOWN: ItemMetaType<MetaUseCooldown, UseCooldown> = typeOf("use_cooldown")

    @JvmField
    val COOLDOWN_GROUP: ItemMetaType<MetaCooldownGroup, Identifier> = typeOf("cooldown_group")

    @JvmField
    val BREW_RECIPE: ItemMetaType<MetaBrewRecipe, String> = typeOf("brew_recipe") {
        serializers {
            registerAll(MetaBrewRecipe.SERIALIZERS)
        }
    }

    @JvmField
    val REMOVED_COMPONENTS: ItemMetaType<MetaRemovedComponents, Set<Key>> = typeOf("removed_components")

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemMetaType]
     */
    private inline fun <reified U : ItemMetaEntry<V>, V> typeOf(id: String, block: ItemMetaType.Builder<U, V>.() -> Unit = {}): ItemMetaType<U, V> {
        val type = ItemMetaType.builder<U, V>().apply(block).build()
        return type.also { BuiltInRegistries.ITEM_META_TYPE.add(id, it) }
    }

    // ------------
    // 内部实现
    // -–----------

    /**
     * 获取一个 [TypeSerializerCollection] 实例, 可用来序列化 [ItemPropContainer] 中的数据类型.
     *
     * 返回的 [TypeSerializerCollection] 仅包含在这里显式声明的序列化操作, 不包含隐式声明的例如 [Int].
     *
     * 该 [TypeSerializerCollection] 的序列化代码被调用的时机发生在 *加载物品配置文件* 时.
     */
    internal fun directSerializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        BuiltInRegistries.ITEM_META_TYPE.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }

}