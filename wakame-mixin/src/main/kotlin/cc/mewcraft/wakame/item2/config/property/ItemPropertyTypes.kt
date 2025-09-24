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

@Deprecated("请使用 ItemPropTypes", ReplaceWith("ItemPropTypes"))
typealias ItemPropertyTypes = ItemPropTypes

/**
 * 该 `object` 包含了所有可用的 [ItemPropertyType].
 *
 * 如果程序员需要为一个 *物品类型* 添加新的数据类型 (包括初始类型), 请在此注册.
 * 如果数据类型是与 *物品堆叠* 所绑定的 (例如: 统计数据), 则不应该考虑此系统.
 *
 * @see cc.mewcraft.wakame.item2.data.ItemDataTypes
 */
data object ItemPropTypes {

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
    val HOLD_LAST_DAMAGE: ItemPropertyType<HoldLastDamage> = typeOf("hold_last_damage")

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

    @JvmField
    val ENTITY_BUCKET: ItemPropertyType<EntityBucket> = typeOf("entity_bucket")

    /**
     * 物品的附魔槽位基本数量.
     *
     * 只有将 configs/config > enchant_slot_base_provider 设置为 "prop" 时才有效.
     */
    @JvmField
    val ENCHANT_SLOT_BASE: ItemPropertyType<Int> = typeOf("enchant_slot_base")

    /**
     * 存在该 prop 表示物品可以用来给一个有附魔槽位的物品添加 1 个额外的附魔槽位.
     */
    @JvmField
    val ENCHANT_SLOT_ADDER: ItemPropertyType<Unit> = typeOf("enchant_slot_adder")

    /**
     * 用于单独设置某个附魔在该物品上所占用的槽位数量 (≠1).
     *
     * 实现上如果不存在该 prop 那么应该直接返回 1 作为某个附魔占用的槽位数量.
     */
    @JvmField
    val ENCHANT_SLOT_CAPACITY: ItemPropertyType<EnchantSlotCapacity> = typeOf("enchant_slot_capacity") {
        serializers {
            registerAll(EnchantSlotCapacity.serializers())
        }
    }

    /**
     * 使物品成为自定义燃料.
     */
    @JvmField
    val FUEL: ItemPropertyType<Fuel> = typeOf("fuel")

    /**
     * 物品放置出来的普通方块.
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes.PLACE_BLOCK
     */
    @JvmField
    val PLACE_BLOCK = typeOf<Identifier>("place_block")

    /**
     * 物品放置出来的流体碰撞方块.
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes.PLACE_LIQUID_COLLISION_BLOCK
     */
    @JvmField
    val PLACE_LIQUID_COLLISION_BLOCK = typeOf<LiquidCollisionBlockSettings>("place_liquid_collision_block")

    /**
     * 物品放置出来的两格高方块.
     *
     * 需要安装对应插件才能正常使用.
     *
     * @see cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes.PLACE_DOUBLE_HIGH_BLOCK
     */
    @JvmField
    val PLACE_DOUBLE_HIGH_BLOCK = typeOf<Identifier>("place_double_high_block")

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