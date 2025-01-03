package cc.mewcraft.wakame.registries

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.attribute.composite.VariableCompositeAttribute
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
    // [机制]
    @JvmField
    val ABILITY: WritableRegistry<Ability> = TODO()

    // [属性]
    @JvmField
    val ATTRIBUTE: WritableRegistry<Attribute> = TODO()

    // [复合属性]
    @JvmField
    val COMPOSITE_ATTRIBUTE: WritableRegistry<CompositeAttributeFacade<ConstantCompositeAttribute, VariableCompositeAttribute>> = TODO()

    // [元素]
    @JvmField
    val ELEMENT: WritableRegistry<Element> = TODO()

    // [实体类型集合]
    @JvmField
    val ENTITY_TYPE_HOLDER: WritableRegistry<EntityTypeHolder> = TODO()

    // [标准物品]
    // 玩家可以直接获得/使用的物品类型
    @JvmField
    val ITEM: WritableRegistry<NekoItem> = TODO()

    // [物品皮肤]
    @JvmField
    val ITEM_SKIN: WritableRegistry<ItemSkin> = TODO()

    // [铭刻]
    @JvmField
    val KIZAMI: WritableRegistry<Kizami>

    // [等级➡稀有度映射]
    @JvmField
    val LEVEL_RARITY_MAPPING: WritableRegistry<LevelMapping>

    // [稀有度]
    @JvmField
    val RARITY: WritableRegistry<Rarity> = TODO()
}