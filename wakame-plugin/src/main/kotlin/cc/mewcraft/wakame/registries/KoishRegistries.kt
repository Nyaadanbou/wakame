package cc.mewcraft.wakame.registries

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.attribute.composite.VariableCompositeAttribute
import cc.mewcraft.wakame.core.DefaultedMappedRegistry
import cc.mewcraft.wakame.core.MappedRegistry
import cc.mewcraft.wakame.core.MutableRegistryAccess
import cc.mewcraft.wakame.core.Registry
import cc.mewcraft.wakame.core.ResourceKey
import cc.mewcraft.wakame.core.WritableRegistry
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.components.ItemSkin
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.LevelMapping
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.CompositeAttributeFacade

object KoishRegistries {
    private val access = MutableRegistryAccess()

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
     * 复合属性.
     */
    @JvmField
    val COMPOSITE_ATTRIBUTE: WritableRegistry<CompositeAttributeFacade<ConstantCompositeAttribute, VariableCompositeAttribute>> = registerSimple(KoishRegistryKeys.COMPOSITE_ATTRIBUTE)

    /**
     * 元素.
     */
    @JvmField
    val ELEMENT: WritableRegistry<Element> = registerDefaulted(KoishRegistryKeys.ELEMENT, "neutral")

    /**
     * 实体类型集合.
     */
    @JvmField
    val ENTITY_TYPE_HOLDER: WritableRegistry<EntityTypeHolder> = registerSimple(KoishRegistryKeys.ENTITY_TYPE_HOLDER)

    /**
     * 标准物品.
     */
    // 玩家可以直接获得/使用的物品类型
    @JvmField
    val ITEM: WritableRegistry<NekoItem> = registerDefaulted(KoishRegistryKeys.ITEM, "error")

    /**
     * 原版套皮物品.
     */
    // 玩家无法直接获得/使用, 仅用于给纯原版物品套一层皮 (i.e., 给原版物品添加内容)
    @JvmField
    val VANILLA_PROXY_ITEM: WritableRegistry<NekoItem> = registerSimple(KoishRegistryKeys.VANILLA_PROXY_ITEM)

    /**
     * 物品皮肤.
     */
    @JvmField
    val ITEM_SKIN: WritableRegistry<ItemSkin> = registerSimple(KoishRegistryKeys.ITEM_SKIN)

    /**
     * 铭刻.
     */
    @JvmField
    val KIZAMI: WritableRegistry<Kizami> = registerSimple(KoishRegistryKeys.KIZAMI)

    /**
     * 等级>稀有度映射.
     */
    @JvmField
    val LEVEL_RARITY_MAPPING: WritableRegistry<LevelMapping> = registerSimple(KoishRegistryKeys.LEVEL_RARITY_MAPPING)

    /**
     * 稀有度.
     */
    @JvmField
    val RARITY: WritableRegistry<Rarity> = registerDefaulted(KoishRegistryKeys.RARITY, "common")

    ///

    private fun <T> registerSimple(key: ResourceKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return access.register(key, MappedRegistry(key).apply(initializer))
    }

    private fun <T> registerDefaulted(key: ResourceKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return access.register(key, DefaultedMappedRegistry(defaultId, key).apply(initializer))
    }
}