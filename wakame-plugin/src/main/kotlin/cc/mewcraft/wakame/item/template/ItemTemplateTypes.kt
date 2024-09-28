package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.components.ItemAttack
import cc.mewcraft.wakame.item.templates.components.*
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object ItemTemplateTypes {
    /**
     * 组件: n/a
     */
    @JvmField
    val ARROW: ItemTemplateType<ItemArrow> = ItemArrow.codec(ItemConstants.ARROW).register()

    /**
     * 组件: n/a
     */
    @JvmField
    val ATTACK: ItemTemplateType<ItemAttack> = ItemAttack.codec(ItemConstants.ATTACK).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAttackSpeed]
     */
    @JvmField
    val ATTACK_SPEED: ItemTemplateType<ItemAttackSpeed> = ItemAttackSpeed.codec(ItemConstants.ATTACK_SPEED).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAttributeModifiers]
     */
    @JvmField
    val ATTRIBUTE_MODIFIERS: ItemTemplateType<ItemAttributeModifiers> = ItemAttributeModifiers.codec(ItemConstants.ATTRIBUTE_MODIFIERS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAdventurePredicate]
     */
    @JvmField
    val CAN_BREAK: ItemTemplateType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_BREAK).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemAdventurePredicate]
     */
    @JvmField
    val CAN_PLACE_ON: ItemTemplateType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_PLACE_ON).register()

    /**
     * 组件: n/a
     */
    @JvmField
    val CASTABLE: ItemTemplateType<Castable> = Castable.codec(ItemConstants.CASTABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemCells]
     */
    @JvmField
    val CELLS: ItemTemplateType<ItemCells> = ItemCells.codec(ItemConstants.CELLS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemCrate]
     */
    @JvmField
    val CRATE: ItemTemplateType<ItemCrate> = ItemCrate.codec(ItemConstants.CRATE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.CustomName]
     */
    @JvmField
    val CUSTOM_NAME: ItemTemplateType<CustomName> = CustomName.codec(ItemConstants.CUSTOM_NAME).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.Damageable]
     */
    @JvmField
    val DAMAGEABLE: ItemTemplateType<Damageable> = Damageable.codec(ItemConstants.DAMAGEABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemDyeColor]
     */
    @JvmField
    val DYED_COLOR: ItemTemplateType<ItemDyeColor> = ItemDyeColor.codec(ItemConstants.DYED_COLOR).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemElements]
     */
    @JvmField
    val ELEMENTS: ItemTemplateType<ItemElements> = ItemElements.codec(ItemConstants.ELEMENTS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemEnchantments]
     */
    @JvmField
    val ENCHANTMENTS: ItemTemplateType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.ENCHANTMENTS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.FireResistant]
     */
    @JvmField
    val FIRE_RESISTANT: ItemTemplateType<FireResistant> = FireResistant.codec(ItemConstants.FIRE_RESISTANT).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.FoodProperties]
     */
    @JvmField
    val FOOD: ItemTemplateType<FoodProperties> = FoodProperties.codec(ItemConstants.FOOD).register()

    /**
     * 组件: n/a
     */
    @JvmField
    val GLOWABLE: ItemTemplateType<ItemGlowable> = ItemGlowable.codec(ItemConstants.GLOWABLE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.HideTooltip]
     */
    @JvmField
    val HIDE_TOOLTIP: ItemTemplateType<HideTooltip> = HideTooltip.codec(ItemConstants.HIDE_TOOLTIP).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.HideAdditionalTooltip]
     */
    @JvmField
    val HIDE_ADDITIONAL_TOOLTIP: ItemTemplateType<HideAdditionalTooltip> = HideAdditionalTooltip.codec(ItemConstants.HIDE_ADDITIONAL_TOOLTIP).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemName]
     */
    @JvmField
    val ITEM_NAME: ItemTemplateType<ItemName> = ItemName.codec(ItemConstants.ITEM_NAME).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemKizamiz]
     */
    @JvmField
    val KIZAMIZ: ItemTemplateType<ItemKizamiz> = ItemKizamiz.codec(ItemConstants.KIZAMIZ).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemLevel]
     */
    @JvmField
    val LEVEL: ItemTemplateType<ItemLevel> = ItemLevel.codec(ItemConstants.LEVEL).register()

    /**
     * 组件: n/a
     */
    @JvmField
    val LORE: ItemTemplateType<ExtraLore> = ExtraLore.codec(ItemConstants.LORE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.PortableCore]
     */
    @JvmField
    val PORTABLE_CORE: ItemTemplateType<PortableCore> = PortableCore.codec(ItemConstants.PORTABLE_CORE).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemRarity]
     */
    @JvmField
    val RARITY: ItemTemplateType<ItemRarity> = ItemRarity.codec(ItemConstants.RARITY).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ItemEnchantments]
     */
    @JvmField
    val STORED_ENCHANTMENTS: ItemTemplateType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.STORED_ENCHANTMENTS).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.Tool]
     */
    @JvmField
    val TOOL: ItemTemplateType<Tool> = Tool.codec(ItemConstants.TOOL).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.ArmorTrim]
     */
    @JvmField
    val TRIM: ItemTemplateType<ArmorTrim> = ArmorTrim.codec(ItemConstants.TRIM).register()

    /**
     * 组件: [cc.mewcraft.wakame.item.components.Unbreakable]
     */
    @JvmField
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