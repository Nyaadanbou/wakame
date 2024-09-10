package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.templates.components.*
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object ItemTemplateTypes {
    /**
     * 组件: n/a
     */
    val ARROW: ItemTemplateType<ItemArrow> = ItemArrow.codec(ItemConstants.ARROW).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAttackSpeed]
     */
    val ATTACK_SPEED: ItemTemplateType<ItemAttackSpeed> = ItemAttackSpeed.codec(ItemConstants.ATTACK_SPEED).register()

    /**
     * 组件: n/a
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val ATTRIBUTABLE: ItemTemplateType<Attributable> = Attributable.codec(ItemConstants.ATTRIBUTABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAttributeModifiers]
     */
    val ATTRIBUTE_MODIFIERS: ItemTemplateType<ItemAttributeModifiers> = ItemAttributeModifiers.codec(ItemConstants.ATTRIBUTE_MODIFIERS).register()

    /**
     * 组件: n/a
     */
    val BOW: ItemTemplateType<ItemBow> = ItemBow.codec(ItemConstants.BOW).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAdventurePredicate]
     */
    val CAN_BREAK: ItemTemplateType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_BREAK).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAdventurePredicate]
     */
    val CAN_PLACE_ON: ItemTemplateType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_PLACE_ON).register()

    /**
     * 组件: n/a
     */
    val CASTABLE: ItemTemplateType<Castable> = Castable.codec(ItemConstants.CASTABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemCells]
     */
    val CELLS: ItemTemplateType<ItemCells> = ItemCells.codec(ItemConstants.CELLS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemCrate]
     */
    val CRATE: ItemTemplateType<ItemCrate> = ItemCrate.codec(ItemConstants.CRATE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.CustomName]
     */
    val CUSTOM_NAME: ItemTemplateType<CustomName> = CustomName.codec(ItemConstants.CUSTOM_NAME).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.Damageable]
     */
    val DAMAGEABLE: ItemTemplateType<Damageable> = Damageable.codec(ItemConstants.DAMAGEABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemDyeColor]
     */
    val DYED_COLOR: ItemTemplateType<ItemDyeColor> = ItemDyeColor.codec(ItemConstants.DYED_COLOR).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemElements]
     */
    val ELEMENTS: ItemTemplateType<ItemElements> = ItemElements.codec(ItemConstants.ELEMENTS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemEnchantments]
     */
    val ENCHANTMENTS: ItemTemplateType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.ENCHANTMENTS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.FireResistant]
     */
    val FIRE_RESISTANT: ItemTemplateType<FireResistant> = FireResistant.codec(ItemConstants.FIRE_RESISTANT).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.FoodProperties]
     */
    val FOOD: ItemTemplateType<FoodProperties> = FoodProperties.codec(ItemConstants.FOOD).register()

    /**
     * 组件: n/a
     */
    val GLOWABLE: ItemTemplateType<ItemGlowable> = ItemGlowable.codec(ItemConstants.GLOWABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.HideTooltip]
     */
    val HIDE_TOOLTIP: ItemTemplateType<HideTooltip> = HideTooltip.codec(ItemConstants.HIDE_TOOLTIP).register()

    /**ni
     * 组件: [cc.mewcraft.wakame.item.components.HideAdditionalTooltip]
     */
    val HIDE_ADDITIONAL_TOOLTIP: ItemTemplateType<HideAdditionalTooltip> = HideAdditionalTooltip.codec(ItemConstants.HIDE_ADDITIONAL_TOOLTIP).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemName]
     */
    val ITEM_NAME: ItemTemplateType<ItemName> = ItemName.codec(ItemConstants.ITEM_NAME).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemKizamiz]
     */
    val KIZAMIZ: ItemTemplateType<ItemKizamiz> = ItemKizamiz.codec(ItemConstants.KIZAMIZ).register()

    /**
     * 组件: n/a
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val KIZAMIABLE: ItemTemplateType<Kizamiable> = Kizamiable.codec(ItemConstants.KIZAMIABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemLevel]
     */
    val LEVEL: ItemTemplateType<ItemLevel> = ItemLevel.codec(ItemConstants.LEVEL).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ExtraLore]
     */
    val LORE: ItemTemplateType<ExtraLore> = ExtraLore.codec(ItemConstants.LORE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.PortableCore]
     */
    val PORTABLE_CORE: ItemTemplateType<PortableCore> = PortableCore.codec(ItemConstants.PORTABLE_CORE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemRarity]
     */
    val RARITY: ItemTemplateType<ItemRarity> = ItemRarity.codec(ItemConstants.RARITY).register()

    /**
     * 组件: n/a
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val SKILLFUL: ItemTemplateType<Skillful> = Skillful.codec(ItemConstants.SKILLFUL).register()

    /**
     * 组件: [ItemEnchantments]
     */
    val STORED_ENCHANTMENTS: ItemTemplateType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.STORED_ENCHANTMENTS).register()

    /**
     * 组件: [Tool]
     */
    val TOOL: ItemTemplateType<Tool> = Tool.codec(ItemConstants.TOOL).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ArmorTrim]
     */
    val TRIM: ItemTemplateType<ArmorTrim> = ArmorTrim.codec(ItemConstants.TRIM).register()

    /**
     * 组件: [Unbreakable]
     */
    val UNBREAKABLE: ItemTemplateType<Unbreakable> = Unbreakable.codec(ItemConstants.UNBREAKABLE).register()

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