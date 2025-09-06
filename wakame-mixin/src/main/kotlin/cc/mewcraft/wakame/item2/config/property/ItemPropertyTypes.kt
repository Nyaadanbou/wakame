package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.item2.config.property.impl.*
import cc.mewcraft.wakame.item2.config.property.impl.weapon.DualSword
import cc.mewcraft.wakame.item2.config.property.impl.weapon.Katana
import cc.mewcraft.wakame.item2.config.property.impl.weapon.Melee
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
    val MELEE: ItemPropertyType<Melee> = typeOf("melee")

    @JvmField
    val DUAL_SWORD: ItemPropertyType<DualSword> = typeOf("dual_sword")

    @JvmField
    val TRIDENT: ItemPropertyType<Unit> = typeOf("trident")

    /**
     * 存在该 property 则表示玩家可以对一个物品发起收购操作.
     *
     * 设计哲学:
     *
     * 玩家发起收购时, 如果要收购的物品涉及到动态数据 (如附魔), 他通常无法完整描述所有的动态数据.
     * 比如, 也许玩家只想收购有锋利V的钻石剑, 但他并不关心再多几个其他的魔咒, 也不关心耐久度.
     * 除非说我们做一个专门的收购系统, 可以精确的描述物品上可能存在的所有动态数据.
     * 但这样的系统似乎有点复杂, 我们暂时不考虑.
     *
     * 因此, 最终的设计是, 对于不太适合收购的物品类型来说, 我们直接禁止玩家发起收购操作.
     * 当然 “禁止发起收购”的逻辑需要在特定的代码路径中实现, 这个 property 只是标记而已.
     */
    @JvmField
    val PLAYER_PURCHASABLE: ItemPropertyType<Unit> = typeOf("player_purchasable")

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