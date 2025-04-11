package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item2.config.property.impl.*
import cc.mewcraft.wakame.item2.config.property.impl.weapon.Katana
import cc.mewcraft.wakame.item2.config.property.impl.weapon.Spear
import cc.mewcraft.wakame.item2.config.property.impl.weapon.Weapon
import cc.mewcraft.wakame.item2.display.SlotDisplayDictData
import cc.mewcraft.wakame.item2.display.SlotDisplayLoreData
import cc.mewcraft.wakame.item2.display.SlotDisplayNameData
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.serializer.holderByNameTypeSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 该 `object` 包含了所有可用的 [ItemPropertyType].
 *
 * 如果程序员需要为一个 *物品类型* 添加新的数据类型 (包括初始类型), 请在此注册.
 * 如果数据类型是与 *物品堆叠* 所绑定的 (例如: 统计数据), 则不应该考虑此系统.
 *
 * @see cc.mewcraft.wakame.item2.data.ItemDataTypes
 */
data object ItemPropertyTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val ID: ItemPropertyType<Identifier> = typeOf("id")

    @JvmField
    val BASE: ItemPropertyType<ItemBase> = typeOf("base") {
        serializers {
            registerExact<ItemBase>(ItemBase.SERIALIZER)
        }
    }

    @JvmField
    val NAME: ItemPropertyType<Component> = typeOf("name")

    @JvmField
    val SLOT: ItemPropertyType<ItemSlotGroup> = typeOf("slot") {
        serializers {
            registerExact(ItemSlot.SERIALIZER)
            registerExact(ItemSlotGroup.SERIALIZER)
        }
    }

    @JvmField
    val HIDDEN: ItemPropertyType<Unit> = typeOf("hidden")

    @JvmField
    val ARROW: ItemPropertyType<Arrow> = typeOf("arrow")

    @JvmField
    val CASTABLE: ItemPropertyType<Unit> = typeOf("castable")

    @JvmField
    val GLOWABLE: ItemPropertyType<Unit> = typeOf("glowable")

    @JvmField
    val EXTRA_LORE: ItemPropertyType<ExtraLore> = typeOf("extra_lore")

    @JvmField
    val ABILITY: ItemPropertyType<AbilityOnItem> = typeOf("ability") {
        serializers {
            register(AbilityTriggerVariant.SERIALIZER)
        }
    }

    @JvmField
    val SLOT_DISPLAY_DICT: ItemPropertyType<SlotDisplayDictData> = typeOf("slot_display_dict")

    @JvmField
    val SLOT_DISPLAY_NAME: ItemPropertyType<SlotDisplayNameData> = typeOf("slot_display_name")

    @JvmField
    val SLOT_DISPLAY_LORE: ItemPropertyType<SlotDisplayLoreData> = typeOf("slot_display_lore") {
        serializers {
            register(SlotDisplayLoreData.SERIALIZER)
        }
    }

    @JvmField
    val COOLDOWN_GROUP: ItemPropertyType<Identifier> = typeOf("cooldown_group")

    @JvmField
    val ATTACK_SPEED: ItemPropertyType<RegistryEntry<AttackSpeed>> = typeOf("attack_speed") {
        serializers {
            register(BuiltInRegistries.ATTACK_SPEED.holderByNameTypeSerializer())
        }
    }

    @JvmField
    val KATANA: ItemPropertyType<Katana> = typeOf("katana")

    @JvmField
    val AXE: ItemPropertyType<Weapon> = typeOf("axe")

    @JvmField
    val CUDGEL: ItemPropertyType<Weapon> = typeOf("cudgel")

    @JvmField
    val HAMMER: ItemPropertyType<Weapon> = typeOf("hammer")

    @JvmField
    val SPEAR: ItemPropertyType<Spear> = typeOf("spear")

    @JvmField
    val SWORD: ItemPropertyType<Weapon> = typeOf("sword")

    @JvmField
    val TRIDENT: ItemPropertyType<Weapon> = typeOf("trident")

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [ItemPropertyType]
     */
    private inline fun <reified T> typeOf(id: String, block: ItemPropertyType.Builder<T>.() -> Unit = {}): ItemPropertyType<T> {
        val type = ItemPropertyType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { BuiltInRegistries.ITEM_PROPERTY_TYPE.add(id, it) }
    }

    // ------------
    // 内部实现
    // -–----------

    /**
     * 获取一个 [TypeSerializerCollection] 实例, 可用来序列化 [ItemPropertyContainer] 中的数据类型.
     *
     * 返回的 [TypeSerializerCollection] 仅包含在这里显式声明的序列化操作, 不包含隐式声明的例如 [Int].
     *
     * 该 [TypeSerializerCollection] 的序列化代码被调用的时机发生在 *加载物品配置文件* 时.
     */
    internal fun directSerializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        BuiltInRegistries.ITEM_PROPERTY_TYPE.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }

}