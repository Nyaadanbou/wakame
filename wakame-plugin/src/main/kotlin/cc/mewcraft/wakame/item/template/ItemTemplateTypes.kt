package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.ItemComponentConstants
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
import cc.mewcraft.wakame.item.components.ItemBow
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemCrate
import cc.mewcraft.wakame.item.components.ItemDyeColor
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
    val ARROW: ItemTemplateType<ItemArrow.Template> = ItemArrow.templateType(ItemComponentConstants.ARROW).register()

    /**
     * 组件: [Attributable]
     */
    val ATTRIBUTABLE: ItemTemplateType<Attributable.Template> = Attributable.templateType(ItemComponentConstants.ATTRIBUTABLE).register()

    /**
     * 组件: [ItemAttributeModifiers]
     */
    val ATTRIBUTE_MODIFIERS: ItemTemplateType<ItemAttributeModifiers.Template> = ItemAttributeModifiers.templateType(ItemComponentConstants.ATTRIBUTE_MODIFIERS).register()

    /**
     * 组件: [ItemBow]
     */
    val BOW: ItemTemplateType<ItemBow.Template> = ItemBow.templateType(ItemComponentConstants.BOW).register()

    /**
     * 组件: [ItemAdventurePredicate]
     */
    val CAN_BREAK: ItemTemplateType<ItemAdventurePredicate.Template> = ItemAdventurePredicate.templateType(ItemComponentConstants.CAN_BREAK).register()

    /**
     * 组件: [ItemAdventurePredicate]
     */
    val CAN_PLACE_ON: ItemTemplateType<ItemAdventurePredicate.Template> = ItemAdventurePredicate.templateType(ItemComponentConstants.CAN_PLACE_ON).register()

    /**
     * 组件: [Castable]
     */
    val CASTABLE: ItemTemplateType<Castable.Template> = Castable.templateType(ItemComponentConstants.CASTABLE).register()

    /**
     * 组件: [ItemCells]
     */
    val CELLS: ItemTemplateType<ItemCells.Template> = ItemCells.templateType(ItemComponentConstants.CELLS).register()

    /**
     * 组件: [ItemCrate]
     */
    val CRATE: ItemTemplateType<ItemCrate.Template> = ItemCrate.templateType(ItemComponentConstants.CRATE).register()

    /**
     * 组件: [CustomName]
     */
    val CUSTOM_NAME: ItemTemplateType<CustomName.Template> = CustomName.templateType(ItemComponentConstants.CUSTOM_NAME).register()

    /**
     * 组件: [Damageable]
     */
    val DAMAGEABLE: ItemTemplateType<Damageable.Template> = Damageable.templateType(ItemComponentConstants.DAMAGEABLE).register()

    /**
     * 组件: [ArmorTrim]
     */
    val DYED_COLOR: ItemTemplateType<ItemDyeColor.Template> = ItemDyeColor.templateType(ItemComponentConstants.DYED_COLOR).register()

    /**
     * 组件: [ItemElements]
     */
    val ELEMENTS: ItemTemplateType<ItemElements.Template> = ItemElements.templateType(ItemComponentConstants.ELEMENTS).register()

    /**
     * 组件: [ItemEnchantments]
     */
    val ENCHANTMENTS: ItemTemplateType<ItemEnchantments.Template> = ItemEnchantments.templateType(ItemComponentConstants.ENCHANTMENTS).register()

    /**
     * 组件: [FireResistant]
     */
    val FIRE_RESISTANT: ItemTemplateType<FireResistant.Template> = FireResistant.templateType(ItemComponentConstants.FIRE_RESISTANT).register()

    /**
     * 组件: [FoodProperties]
     */
    val FOOD: ItemTemplateType<FoodProperties.Template> = FoodProperties.templateType(ItemComponentConstants.FOOD).register()

    /**
     * 组件: [HideTooltip]
     */
    val HIDE_TOOLTIP: ItemTemplateType<HideTooltip.Template> = HideTooltip.templateType(ItemComponentConstants.HIDE_TOOLTIP).register()

    /**ni
     * 组件: [HideAdditionalTooltip]
     */
    val HIDE_ADDITIONAL_TOOLTIP: ItemTemplateType<HideAdditionalTooltip.Template> = HideAdditionalTooltip.templateType(ItemComponentConstants.HIDE_ADDITIONAL_TOOLTIP).register()

    /**
     * 组件: [ItemName]
     */
    val ITEM_NAME: ItemTemplateType<ItemName.Template> = ItemName.templateType(ItemComponentConstants.ITEM_NAME).register()

    /**
     * 组件: [ItemKizamiz]
     */
    val KIZAMIZ: ItemTemplateType<ItemKizamiz.Template> = ItemKizamiz.templateType(ItemComponentConstants.KIZAMIZ).register()

    /**
     * 组件: [Kizamiable]
     */
    val KIZAMIABLE: ItemTemplateType<Kizamiable.Template> = Kizamiable.templateType(ItemComponentConstants.KIZAMIABLE).register()

    /**
     * 组件: [ItemLevel]
     */
    val LEVEL: ItemTemplateType<ItemLevel.Template> = ItemLevel.templateType(ItemComponentConstants.LEVEL).register()

    /**
     * 组件: [ExtraLore]
     */
    val LORE: ItemTemplateType<ExtraLore.Template> = ExtraLore.templateType(ItemComponentConstants.LORE).register()

    /**
     * 组件: [ItemRarity]
     */
    val RARITY: ItemTemplateType<ItemRarity.Template> = ItemRarity.templateType(ItemComponentConstants.RARITY).register()

    /**
     * 组件: [Skillful]
     */
    val SKILLFUL: ItemTemplateType<Skillful.Template> = Skillful.templateType(ItemComponentConstants.SKILLFUL).register()

    /**
     * 组件: [ItemEnchantments]
     */
    val STORED_ENCHANTMENTS: ItemTemplateType<ItemEnchantments.Template> = ItemEnchantments.templateType(ItemComponentConstants.STORED_ENCHANTMENTS).register()

    /**
     * 组件: [Tool]
     */
    val TOOL: ItemTemplateType<Tool.Template> = Tool.templateType(ItemComponentConstants.TOOL).register()

    /**
     * 组件: [ArmorTrim]
     */
    val TRIM: ItemTemplateType<ArmorTrim.Template> = ArmorTrim.templateType(ItemComponentConstants.TRIM).register()

    /**
     * 组件: [Unbreakable]
     */
    val UNBREAKABLE: ItemTemplateType<Unbreakable.Template> = Unbreakable.templateType(ItemComponentConstants.UNBREAKABLE).register()

    /**
     * 获取所有模板的序列化器.
     */
    internal fun collectTypeSerializers(): TypeSerializerCollection {
        return ItemTemplateTypeHelper.serializerBuilder.build()
    }

    internal fun <T : ItemTemplate<*>> ItemTemplateType<T>.register(): ItemTemplateType<T> {
        ItemTemplateTypeHelper.serializerBuilder.registerAll(this.childrenCodecs())
        return this
    }
}

private object ItemTemplateTypeHelper {
    val serializerBuilder: TypeSerializerCollection.Builder = TypeSerializerCollection.builder()
}