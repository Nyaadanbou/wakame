package cc.mewcraft.wakame.core.registries

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.attribute.composite.VariableCompositeAttribute
import cc.mewcraft.wakame.core.DefaultedWritableRegistry
import cc.mewcraft.wakame.core.MutableRegistryAccess
import cc.mewcraft.wakame.core.Registry
import cc.mewcraft.wakame.core.RegistryKey
import cc.mewcraft.wakame.core.SimpleDefaultedRegistry
import cc.mewcraft.wakame.core.SimpleRegistry
import cc.mewcraft.wakame.core.WritableRegistry
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.components.ItemSkin
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.rarity.LevelMapping
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.CompositeAttributeFacade
import cc.mewcraft.wakame.world.entity.EntityTypeHolder

object KoishRegistries {
    private val ACCESS = MutableRegistryAccess()

    ///

    /**
     * 机制.
     */
    @JvmField
    val ABILITY: WritableRegistry<Ability> = registerSimple(KoishRegistryKeys.ABILITY)

    /**
     * 属性.
     */
    @JvmField
    val ATTRIBUTE: WritableRegistry<Attribute> = registerSimple(KoishRegistryKeys.ATTRIBUTE)

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
    val ATTRIBUTE_SUPPLIER: WritableRegistry<AttributeSupplier> = registerSimple(KoishRegistryKeys.ATTRIBUTE_SUPPLIER)

    /**
     * 复合属性.
     */
    @JvmField
    val COMPOSITE_ATTRIBUTE: WritableRegistry<CompositeAttributeFacade<ConstantCompositeAttribute, VariableCompositeAttribute>> = registerSimple(KoishRegistryKeys.COMPOSITE_ATTRIBUTE)

    /**
     * 元素.
     */
    @JvmField
    val ELEMENT: DefaultedWritableRegistry<Element> = registerDefaulted(KoishRegistryKeys.ELEMENT, "neutral")

    /**
     * 实体类型集合.
     */
    @JvmField
    val ENTITY_TYPE_HOLDER: WritableRegistry<EntityTypeHolder> = registerSimple(KoishRegistryKeys.ENTITY_TYPE_HOLDER)

    /**
     * 标准物品.
     *
     * 玩家可以直接获得/使用的物品类型.
     */
    @JvmField
    val ITEM: DefaultedWritableRegistry<NekoItem> = registerDefaulted(KoishRegistryKeys.ITEM, "unknown")

    /**
     * 原版套皮物品.
     *
     * 玩家无法直接获得/使用, 仅用于给纯原版物品套一层皮 (i.e., 给原版物品添加内容)
     */
    @JvmField
    val VANILLA_WRAPPER_ITEM: WritableRegistry<NekoItem> = registerSimple(KoishRegistryKeys.VANILLA_PROXY_ITEM)

    /**
     * 物品皮肤.
     */
    @JvmField
    val ITEM_SKIN: WritableRegistry<ItemSkin> = registerSimple(KoishRegistryKeys.ITEM_SKIN)

    /**
     * 铭刻.
     */
    @JvmField
    val KIZAMI: WritableRegistry<KizamiType> = registerSimple(KoishRegistryKeys.KIZAMI)

    /**
     * 等级>稀有度映射.
     */
    @JvmField
    val LEVEL_RARITY_MAPPING: WritableRegistry<LevelMapping> = registerSimple(KoishRegistryKeys.LEVEL_RARITY_MAPPING)

    /**
     * 稀有度.
     */
    @JvmField
    val RARITY: DefaultedWritableRegistry<Rarity> = registerDefaulted(KoishRegistryKeys.RARITY, "common")

    ///

    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

    private fun <T> registerSimple(key: RegistryKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return ACCESS.add(key, SimpleRegistry(key).apply(initializer))
    }

    private fun <T> registerDefaulted(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): DefaultedWritableRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedRegistry(defaultId, key).apply(initializer)).asDefaultedWritable()
    }
}