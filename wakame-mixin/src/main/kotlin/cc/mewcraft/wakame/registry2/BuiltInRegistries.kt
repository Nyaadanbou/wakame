package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.meta.AbilityMetaTypes
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeSupplier
import cc.mewcraft.wakame.entity.attribute.ImaginaryAttributeMap
import cc.mewcraft.wakame.entity.attribute.bundle.AttributeFacade
import cc.mewcraft.wakame.entity.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.entity.attribute.bundle.VariableAttributeBundle
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.entity.typeref.EntityRef
import cc.mewcraft.wakame.item2.ItemRefHandler
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishItemProxy
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.CoreType
import cc.mewcraft.wakame.item2.data.impl.CoreTypes
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.loot.entry.LootPoolEntries
import cc.mewcraft.wakame.loot.entry.LootPoolEntryType
import cc.mewcraft.wakame.loot.predicate.LootPredicateType
import cc.mewcraft.wakame.loot.predicate.LootPredicates
import cc.mewcraft.wakame.rarity2.LevelToRarityMapping
import cc.mewcraft.wakame.rarity2.Rarity
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly

/**
 * 这些 [Registry] 内的注册项是 *不可变的* - 无法在游戏运行时添加或删除注册项.
 */
object BuiltInRegistries {

    private val ACCESS: MutableRegistryAccess = MutableRegistryAccess()
    private val INITIALIZERS = mutableListOf<() -> Unit>()

    // ------------
    // 注册表
    // ------------

    /**
     * 技能配置.
     */
    @JvmField
    val ABILITY_META: WritableRegistry<AbilityMeta> = registerSimple(BuiltInRegistryKeys.ABILITY_META)

    /**
     * 技能配置的类型.
     */
    @JvmField
    val ABILITY_META_TYPE: WritableRegistry<AbilityMetaType<*>> = registerSimple(BuiltInRegistryKeys.ABILITY_META_TYPE) { AbilityMetaTypes }

    /**
     * 技能触发器.
     */
    @JvmField
    val ABILITY_TRIGGER: WritableRegistry<AbilityTrigger> = registerSimple(BuiltInRegistryKeys.ABILITY_TRIGGER)

    /**
     * 自定义物品的类型.
     */
    @JvmField
    val ITEM: WritableDefaultedFuzzyRegistry<KoishItem> = registerDefaultedFuzzy(BuiltInRegistryKeys.ITEM, "internal/error")

    /**
     * 套皮物品堆叠的实例.
     */
    @JvmField
    val ITEM_PROXY: WritableRegistry<KoishItemProxy> = registerSimple(BuiltInRegistryKeys.ITEM_PROXY)

    /**
     * "Item Data" 的类型.
     */
    @JvmField
    val ITEM_DATA_TYPE: WritableRegistry<ItemDataType<*>> = registerSimple(BuiltInRegistryKeys.ITEM_DATA_TYPE) { ItemDataTypes }

    /**
     * "Item Meta" 的类型.
     * "Item Meta" 相当于 "Item Data" 的配置文件.
     */
    @JvmField
    val ITEM_META_TYPE: WritableRegistry<ItemMetaType<*, *>> = registerSimple(BuiltInRegistryKeys.ITEM_META_TYPE) { ItemMetaTypes }

    /**
     * "Item Property" 的类型.
     */
    @JvmField
    val ITEM_PROPERTY_TYPE: WritableRegistry<ItemPropertyType<*>> = registerSimple(BuiltInRegistryKeys.ITEM_PROPERTY_TYPE) { ItemPropertyTypes }

    /**
     * "Item Behavior" 的类型.
     */
    @JvmField
    val ITEM_BEHAVIOR: WritableRegistry<ItemBehavior> = registerSimple(BuiltInRegistryKeys.ITEM_BEHAVIOR) { ItemBehaviorTypes }

    /**
     * [ItemRefHandler] 的外部实例. 包含来自第三方物品系统 (如 Brewery) 的 [ItemRefHandler].
     */
    @JvmField
    val ITEM_REF_HANDLER_EXTERNAL: WritableRegistry<ItemRefHandler<*>> = registerSimple(BuiltInRegistryKeys.ITEM_REF_HANDLER)

    /**
     * [ItemRefHandler] 的内置实例. 只包含 Koish 和 Minecraft 两个系统的 [ItemRefHandler].
     */
    @ApiStatus.Internal
    @JvmField
    val ITEM_REF_HANDLER_INTERNAL: WritableDefaultedRegistry<ItemRefHandler<*>> = registerDefaulted(BuiltInRegistryKeys.ITEM_REF_HANDLER_INTERNAL, "minecraft")

    /**
     * 稀有度的类型.
     */
    @JvmField
    val RARITY: WritableDefaultedRegistry<Rarity> = registerDefaulted(BuiltInRegistryKeys.RARITY, "common")

    /**
     * 铭刻的类型.
     */
    @JvmField
    val KIZAMI: WritableRegistry<Kizami> = registerSimple(BuiltInRegistryKeys.KIZAMI)

    /**
     * 实体类型的引用.
     */
    @JvmField
    val ENTITY_REF: WritableRegistry<EntityRef> = registerSimple(BuiltInRegistryKeys.ENTITY_REF)

    /**
     * 元素的类型.
     */
    @JvmField
    val ELEMENT: WritableDefaultedRegistry<Element> = registerDefaulted(BuiltInRegistryKeys.ELEMENT, "neutral") // = koish:neutral

    /**
     * 等级->稀有度的映射.
     */
    @JvmField
    val LEVEL_TO_RARITY_MAPPING: WritableDefaultedRegistry<LevelToRarityMapping> = registerDefaulted(BuiltInRegistryKeys.LEVEL_TO_RARITY_MAPPING, "__default__") // = koish:__default__

    /**
     * 属性的类型.
     */
    @JvmField
    val ATTRIBUTE: WritableRegistry<Attribute> = registerSimple(BuiltInRegistryKeys.ATTRIBUTE)

    /**
     * 实体的默认属性.
     *
     * 1. 包含所有原版实体(包括玩家)的默认属性.
     * 2. MythicMobs 生物的属性不由这里提供.
     *
     * ### Notes
     * Using [RegistryKey] to identify the "type" of living entities because we want the whole
     * attribute system to be compatible with 3rd party mob system such as MythicMobs,
     * in which case the enum type is not enough to express all types.
     */
    @JvmField
    val ATTRIBUTE_SUPPLIER: WritableRegistry<AttributeSupplier> = registerSimple(BuiltInRegistryKeys.ATTRIBUTE_SUPPLIER)

    /**
     * 属性块 [cc.mewcraft.wakame.entity.attribute.bundle.AttributeBundle] 的外观, 用于访问属性块相关的数据和逻辑.
     */
    @JvmField
    val ATTRIBUTE_FACADE: WritableRegistry<AttributeFacade<ConstantAttributeBundle, VariableAttributeBundle>> = registerSimple(BuiltInRegistryKeys.ATTRIBUTE_FACADE)

    /**
     * 虚构的属性映射.
     */
    @JvmField
    val IMG_ATTRIBUTE_MAP: WritableRegistry<ImaginaryAttributeMap> = registerSimple(BuiltInRegistryKeys.IMG_ATTRIBUTE_MAP)

    /**
     * 核心的类型.
     */
    @JvmField
    val CORE_TYPE: WritableRegistry<CoreType> = registerSimple(BuiltInRegistryKeys.CORE_TYPE) { CoreTypes }

    /**
     * 攻击速度.
     */
    @JvmField
    val ATTACK_SPEED: WritableDefaultedRegistry<AttackSpeed> = registerDefaulted(BuiltInRegistryKeys.ATTACK_SPEED, "intrinsic")

    /**
     * 战利品表.
     */
    @JvmField
    val LOOT_TABLE: WritableRegistry<LootTable<*>> = registerSimple(BuiltInRegistryKeys.LOOT_TABLE)

    /**
     * LootPoolEntry 的类型.
     */
    @JvmField
    val LOOT_POOL_ENTRY_TYPE: WritableRegistry<LootPoolEntryType<*>> = registerSimple(BuiltInRegistryKeys.LOOT_POOL_ENTRY_TYPE) { LootPoolEntries }

    /**
     * LootPredicate 的类型.
     */
    @JvmField
    val LOOT_PREDICATE_TYPE: WritableRegistry<LootPredicateType<*>> = registerSimple(BuiltInRegistryKeys.LOOT_PREDICATE_TYPE) { LootPredicates }

    // 在本类型 <clinit> 最后执行所有的 INITIALIZER
    init {
        INITIALIZERS.forEach { initializer -> initializer() }
    }


    // ------------
    // 方便函数
    // ------------

    /**
     * 创建一个 [WritableRegistry].
     */
    private fun <T> registerSimple(key: RegistryKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return internalRegister(key, SimpleRegistry(key), initializer)
    }

    /**
     * 创建一个 [WritableDefaultedRegistry].
     */
    private fun <T> registerDefaulted(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedRegistry<T> {
        return internalRegister(key, SimpleDefaultedRegistry(defaultId, key), initializer)
    }

    /**
     * 创建一个 [WritableDefaultedFuzzyRegistry].
     */
    private fun <T> registerDefaultedFuzzy(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedFuzzyRegistry<T> {
        return internalRegister(key, SimpleDefaultedFuzzyRegistry(defaultId, key), initializer)
    }

    /**
     * 创建一个 [R].
     */
    private fun <T, R : WritableRegistry<T>> internalRegister(key: RegistryKey<out Registry<T>>, registry: R, initializer: (Registry<T>) -> Unit = {}): R {
        ACCESS.add(key, registry).also { INITIALIZERS += { initializer(it) } }
        return registry
    }

    // ------------
    // 仅测试用
    // ------------

    @TestOnly
    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

}
