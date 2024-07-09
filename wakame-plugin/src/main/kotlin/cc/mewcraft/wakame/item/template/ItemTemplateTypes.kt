package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.components.ArmorTrim
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Castable
import cc.mewcraft.wakame.item.components.CustomName
import cc.mewcraft.wakame.item.components.Damageable
import cc.mewcraft.wakame.item.components.ExtraLore
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.HideAdditionalTooltip
import cc.mewcraft.wakame.item.components.HideTooltip
import cc.mewcraft.wakame.item.components.ItemAdventurePredicate
import cc.mewcraft.wakame.item.components.ItemArrow
import cc.mewcraft.wakame.item.components.ItemAttributeModifiers
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemCrate
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemEnchantments
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemName
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.Kizamiable
import cc.mewcraft.wakame.item.components.Skillful
import cc.mewcraft.wakame.item.components.Tool
import cc.mewcraft.wakame.item.components.Unbreakable
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object ItemTemplateTypes {
    /**
     * 组件: [ItemArrow]
     */
    val ARROW: ItemTemplateType<ItemArrow.Template> = ItemArrow.templateType().register()

    /**
     * 组件: [Attributable]
     */
    val ATTRIBUTABLE: ItemTemplateType<Attributable.Template> = Attributable.templateType().register()

    /**
     * 组件: [ItemAttributeModifiers]
     */
    val ATTRIBUTE_MODIFIERS: ItemTemplateType<ItemAttributeModifiers.Template> = ItemAttributeModifiers.templateType().register()

    /**
     * 组件: [ItemAdventurePredicate]
     */
    val CAN_BREAK: ItemTemplateType<ItemAdventurePredicate.Template> = ItemAdventurePredicate.templateType().register()

    /**
     * 组件: [ItemAdventurePredicate]
     */
    val CAN_PLACE_ON: ItemTemplateType<ItemAdventurePredicate.Template> = ItemAdventurePredicate.templateType().register()

    /**
     * 组件: [Castable]
     */
    val CASTABLE: ItemTemplateType<Castable.Template> = Castable.templateType().register()

    /**
     * 组件:
     */
    val CELLS: ItemTemplateType<ItemCells.Template> = ItemCells.templateType().register()

    /**
     * 组件: [ItemCrate]
     */
    val CRATE: ItemTemplateType<ItemCrate.Template> = ItemCrate.templateType().register()

    /**
     * 组件: [CustomName]
     */
    val CUSTOM_NAME: ItemTemplateType<CustomName.Template> = CustomName.templateType().register()

    /**
     * 组件: [Damageable]
     */
    val DAMAGEABLE: ItemTemplateType<Damageable.Template> = Damageable.templateType().register()

    /**
     * 组件: [ArmorTrim]
     */
    val DYED_COLOR: ItemTemplateType<ArmorTrim.Template> = ArmorTrim.templateType().register()

    /**
     * 组件: [ItemElements]
     */
    val ELEMENTS: ItemTemplateType<ItemElements.Template> = ItemElements.templateType().register()

    /**
     * 组件: [ItemEnchantments]
     */
    val ENCHANTMENTS: ItemTemplateType<ItemEnchantments.Template> = ItemEnchantments.templateType().register()

    /**
     * 组件: [FireResistant]
     */
    val FIRE_RESISTANT: ItemTemplateType<FireResistant.Template> = FireResistant.templateType().register()

    /**
     * 组件: [FoodProperties]
     */
    val FOOD: ItemTemplateType<FoodProperties.Template> = FoodProperties.templateType().register()

    /**
     * 组件: [HideTooltip]
     */
    val HIDE_TOOLTIP: ItemTemplateType<HideTooltip.Template> = HideTooltip.templateType().register()

    /**ni
     * 组件: [HideAdditionalTooltip]
     */
    val HIDE_ADDITIONAL_TOOLTIP: ItemTemplateType<HideAdditionalTooltip.Template> = HideAdditionalTooltip.templateType().register()

    /**
     * 组件: [ItemName]
     */
    val ITEM_NAME: ItemTemplateType<ItemName.Template> = ItemName.templateType().register()

    /**
     * 组件: [ItemKizamiz]
     */
    val KIZAMIZ: ItemTemplateType<ItemKizamiz.Template> = ItemKizamiz.templateType().register()

    /**
     * 组件: [Kizamiable]
     */
    val KIZAMIABLE: ItemTemplateType<Kizamiable.Template> = Kizamiable.templateType().register()

    /**
     * 组件: [ItemLevel]
     */
    val LEVEL: ItemTemplateType<ItemLevel.Template> = ItemLevel.templateType().register()

    /**
     * 组件: [ExtraLore]
     */
    val LORE: ItemTemplateType<ExtraLore.Template> = ExtraLore.templateType().register()

    /**
     * 组件: [ItemRarity]
     */
    val RARITY: ItemTemplateType<ItemRarity.Template> = ItemRarity.templateType().register()

    /**
     * 组件: [Skillful]
     */
    val SKILLFUL: ItemTemplateType<Skillful.Template> = Skillful.templateType().register()

    // FIXME templateType 也需要有一个 id,
    //  因为有些组件可能会有相同的类型, 但是 id 不同.
    val STORED_ENCHANTMENTS: ItemTemplateType<ItemEnchantments.Template> = ItemEnchantments.templateType().register()

    /**
     * 组件: [Tool]
     */
    val TOOL: ItemTemplateType<Tool.Template> = Tool.templateType().register()

    /**
     * 组件: [ArmorTrim]
     */
    val TRIM: ItemTemplateType<ArmorTrim.Template> = ArmorTrim.templateType().register()

    /**
     * 组件: [Unbreakable]
     */
    val UNBREAKABLE: ItemTemplateType<Unbreakable.Template> = Unbreakable.templateType().register()

    /**
     * 获取所有模板的序列化器.
     */
    internal fun collectTypeSerializers(): TypeSerializerCollection {
        return ItemTemplateTypeHelper.serializerBuilder.build()
    }

    internal fun <T : ItemTemplate<*>> ItemTemplateType<T>.register(): ItemTemplateType<T> {
        ItemTemplateTypeHelper.serializerBuilder.register(this.typeToken, this)
        ItemTemplateTypeHelper.serializerBuilder.registerAll(this.childSerializers())
        return this
    }
}

private object ItemTemplateTypeHelper {
    val serializerBuilder: TypeSerializerCollection.Builder = TypeSerializerCollection.builder()
}