package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.components.*
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object ItemTemplateTypes {
    /**
     * 组件: [ItemArrow]
     */
    val ARROW: ItemTemplateType<ItemArrow.Template> = ItemArrow.templateType(ItemConstants.ARROW).register()

    /**
     * 组件: [ItemAttackSpeed]
     */
    val ATTACK_SPEED: ItemTemplateType<ItemAttackSpeed.Template> = ItemAttackSpeed.templateType(ItemConstants.ATTACK_SPEED).register()

    /**
     * 组件: [Attributable]
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val ATTRIBUTABLE: ItemTemplateType<Attributable.Template> = Attributable.templateType(ItemConstants.ATTRIBUTABLE).register()

    /**
     * 组件: [ItemAttributeModifiers]
     */
    val ATTRIBUTE_MODIFIERS: ItemTemplateType<ItemAttributeModifiers.Template> = ItemAttributeModifiers.templateType(ItemConstants.ATTRIBUTE_MODIFIERS).register()

    /**
     * 组件: [ItemBow]
     */
    val BOW: ItemTemplateType<ItemBow.Template> = ItemBow.templateType(ItemConstants.BOW).register()

    /**
     * 组件: [ItemAdventurePredicate]
     */
    val CAN_BREAK: ItemTemplateType<ItemAdventurePredicate.Template> = ItemAdventurePredicate.templateType(ItemConstants.CAN_BREAK).register()

    /**
     * 组件: [ItemAdventurePredicate]
     */
    val CAN_PLACE_ON: ItemTemplateType<ItemAdventurePredicate.Template> = ItemAdventurePredicate.templateType(ItemConstants.CAN_PLACE_ON).register()

    /**
     * 组件: [Castable]
     */
    val CASTABLE: ItemTemplateType<Castable.Template> = Castable.templateType(ItemConstants.CASTABLE).register()

    /**
     * 组件: [ItemCells]
     */
    val CELLS: ItemTemplateType<ItemCells.Template> = ItemCells.templateType(ItemConstants.CELLS).register()

    /**
     * 组件: [ItemCrate]
     */
    val CRATE: ItemTemplateType<ItemCrate.Template> = ItemCrate.templateType(ItemConstants.CRATE).register()

    /**
     * 组件: [CustomName]
     */
    val CUSTOM_NAME: ItemTemplateType<CustomName.Template> = CustomName.templateType(ItemConstants.CUSTOM_NAME).register()

    /**
     * 组件: [Damageable]
     */
    val DAMAGEABLE: ItemTemplateType<Damageable.Template> = Damageable.templateType(ItemConstants.DAMAGEABLE).register()

    /**
     * 组件: [ArmorTrim]
     */
    val DYED_COLOR: ItemTemplateType<ItemDyeColor.Template> = ItemDyeColor.templateType(ItemConstants.DYED_COLOR).register()

    /**
     * 组件: [ItemElements]
     */
    val ELEMENTS: ItemTemplateType<ItemElements.Template> = ItemElements.templateType(ItemConstants.ELEMENTS).register()

    /**
     * 组件: [ItemEnchantments]
     */
    val ENCHANTMENTS: ItemTemplateType<ItemEnchantments.Template> = ItemEnchantments.templateType(ItemConstants.ENCHANTMENTS).register()

    /**
     * 组件: [FireResistant]
     */
    val FIRE_RESISTANT: ItemTemplateType<FireResistant.Template> = FireResistant.templateType(ItemConstants.FIRE_RESISTANT).register()

    /**
     * 组件: [FoodProperties]
     */
    val FOOD: ItemTemplateType<FoodProperties.Template> = FoodProperties.templateType(ItemConstants.FOOD).register()

    /**
     * 组件: [ItemGlowable]
     */
    val GLOWABLE: ItemTemplateType<ItemGlowable.Template> = ItemGlowable.templateType(ItemConstants.GLOWABLE).register()

    /**
     * 组件: [HideTooltip]
     */
    val HIDE_TOOLTIP: ItemTemplateType<HideTooltip.Template> = HideTooltip.templateType(ItemConstants.HIDE_TOOLTIP).register()

    /**ni
     * 组件: [HideAdditionalTooltip]
     */
    val HIDE_ADDITIONAL_TOOLTIP: ItemTemplateType<HideAdditionalTooltip.Template> = HideAdditionalTooltip.templateType(ItemConstants.HIDE_ADDITIONAL_TOOLTIP).register()

    /**
     * 组件: [ItemName]
     */
    val ITEM_NAME: ItemTemplateType<ItemName.Template> = ItemName.templateType(ItemConstants.ITEM_NAME).register()

    /**
     * 组件: [ItemKizamiz]
     */
    val KIZAMIZ: ItemTemplateType<ItemKizamiz.Template> = ItemKizamiz.templateType(ItemConstants.KIZAMIZ).register()

    /**
     * 组件: [Kizamiable]
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val KIZAMIABLE: ItemTemplateType<Kizamiable.Template> = Kizamiable.templateType(ItemConstants.KIZAMIABLE).register()

    /**
     * 组件: [ItemLevel]
     */
    val LEVEL: ItemTemplateType<ItemLevel.Template> = ItemLevel.templateType(ItemConstants.LEVEL).register()

    /**
     * 组件: [ExtraLore]
     */
    val LORE: ItemTemplateType<ExtraLore.Template> = ExtraLore.templateType(ItemConstants.LORE).register()

    /**
     * 组件: [PortableCore]
     */
    val PORTABLE_CORE: ItemTemplateType<PortableCore.Template> = PortableCore.templateType(ItemConstants.PORTABLE_CORE).register()

    /**
     * 组件: [ItemRarity]
     */
    val RARITY: ItemTemplateType<ItemRarity.Template> = ItemRarity.templateType(ItemConstants.RARITY).register()

    /**
     * 组件: [Skillful]
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val SKILLFUL: ItemTemplateType<Skillful.Template> = Skillful.templateType(ItemConstants.SKILLFUL).register()

    /**
     * 组件: [ItemEnchantments]
     */
    val STORED_ENCHANTMENTS: ItemTemplateType<ItemEnchantments.Template> = ItemEnchantments.templateType(ItemConstants.STORED_ENCHANTMENTS).register()

    /**
     * 组件: [Tool]
     */
    val TOOL: ItemTemplateType<Tool.Template> = Tool.templateType(ItemConstants.TOOL).register()

    /**
     * 组件: [ArmorTrim]
     */
    val TRIM: ItemTemplateType<ArmorTrim.Template> = ArmorTrim.templateType(ItemConstants.TRIM).register()

    /**
     * 组件: [Unbreakable]
     */
    val UNBREAKABLE: ItemTemplateType<Unbreakable.Template> = Unbreakable.templateType(ItemConstants.UNBREAKABLE).register()

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