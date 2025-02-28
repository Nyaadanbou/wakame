package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.ImaginaryAttributeMap
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.attribute.bundle.VariableAttributeBundle
import cc.mewcraft.wakame.catalog.item.Category
import cc.mewcraft.wakame.catalog.item.recipe.LootTableRecipe
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacade
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.components.ItemSkin
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.rarity.LevelRarityMapping
import cc.mewcraft.wakame.rarity.RarityType
import cc.mewcraft.wakame.world.entity.EntityTypeHolder

object KoishRegistries {
    private val ACCESS: MutableRegistryAccess = MutableRegistryAccess()

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
     * 属性块的 Facade.
     */
    @JvmField
    val ATTRIBUTE_BUNDLE_FACADE: WritableRegistry<AttributeBundleFacade<ConstantAttributeBundle, VariableAttributeBundle>> = registerSimple(KoishRegistryKeys.ATTRIBUTE_BUNDLE_FACADE)

    /**
     * 元素.
     */
    @JvmField
    val ELEMENT: WritableDefaultedRegistry<ElementType> = registerDefaulted(KoishRegistryKeys.ELEMENT, "neutral") // = koish:neutral

    /**
     * 实体类型集合.
     */
    @JvmField
    val ENTITY_TYPE_HOLDER: WritableRegistry<EntityTypeHolder> = registerSimple(KoishRegistryKeys.ENTITY_TYPE_HOLDER)

    /**
     * 虚构的属性映射.
     */
    @JvmField
    val IMAGINARY_ATTRIBUTE_MAP: WritableRegistry<ImaginaryAttributeMap> = registerSimple(KoishRegistryKeys.IMAGINARY_ATTRIBUTE_MAP)

    /**
     * 标准物品类型.
     *
     * 玩家可以直接获得/使用的物品类型.
     */
    @JvmField
    val ITEM: WritableDefaultedFuzzyRegistry<NekoItem> = registerDefaultedFuzzy(KoishRegistryKeys.ITEM, "internal:unknown")

    /**
     * 物品皮肤.
     */
    @JvmField
    val ITEM_SKIN: WritableRegistry<ItemSkin> = registerSimple(KoishRegistryKeys.ITEM_SKIN)

    /**
     * 物品图鉴中物品的类别.
     */
    @JvmField
    val ITEM_CATEGORY: WritableRegistry<Category> = registerSimple(KoishRegistryKeys.ITEM_CATEGORY)

    /**
     * 铭刻.
     */
    @JvmField
    val KIZAMI: WritableRegistry<KizamiType> = registerSimple(KoishRegistryKeys.KIZAMI)

    /**
     * 等级>稀有度映射.
     */
    @JvmField
    val LEVEL_RARITY_MAPPING: WritableDefaultedRegistry<LevelRarityMapping> = registerDefaulted(KoishRegistryKeys.LEVEL_RARITY_MAPPING, "__default__") // = koish:__default__

    /**
     * 物品图鉴中的战利品表配方.
     */
    @JvmField
    val LOOT_TABLE_RECIPE: WritableRegistry<LootTableRecipe> = registerSimple(KoishRegistryKeys.LOOT_TABLE_RECIPE)

    /**
     * 稀有度.
     */
    @JvmField
    val RARITY: WritableDefaultedRegistry<RarityType> = registerDefaulted(KoishRegistryKeys.RARITY, "common") // = koish:common

    ///

    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

    /**
     * 创建一个 [WritableRegistry].
     */
    private fun <T> registerSimple(key: RegistryKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return ACCESS.add(key, SimpleRegistry(key).apply(initializer))
    }

    /**
     * 创建一个 [WritableDefaultedRegistry].
     */
    private fun <T> registerDefaulted(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedRegistry(defaultId, key).apply(initializer)) as WritableDefaultedRegistry<T>
    }

    /**
     * 创建一个 [WritableDefaultedFuzzyRegistry].
     */
    private fun <T> registerDefaultedFuzzy(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedFuzzyRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedFuzzyRegistry(defaultId, key).apply(initializer)) as WritableDefaultedFuzzyRegistry<T>
    }
}