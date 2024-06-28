package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.item.components.Arrow
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Castable
import cc.mewcraft.wakame.item.components.Crate
import cc.mewcraft.wakame.item.components.CustomName
import cc.mewcraft.wakame.item.components.Damageable
import cc.mewcraft.wakame.item.components.ExtraLore
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemName
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.ItemTracks
import cc.mewcraft.wakame.item.components.Kizamiable
import cc.mewcraft.wakame.item.components.Skillful
import cc.mewcraft.wakame.item.components.Tool
import cc.mewcraft.wakame.item.components.Unbreakable

object ItemTemplateTypes {
    /**
     * 组件: [Arrow]
     */
    val ARROW: ItemTemplateType<Arrow.Template> = Arrow.Template

    /**
     * 组件: [Attributable]
     */
    val ATTRIBUTABLE: ItemTemplateType<Attributable.Template> = Attributable.Template

    /**
     * 组件: [Castable]
     */
    val CASTABLE: ItemTemplateType<Castable.Template> = Castable.Template

    /**
     * 组件:
     */
    val CELLS: ItemTemplateType<ItemCells.Template> = ItemCells.Template

    /**
     * 组件: [Crate]
     */
    val CRATE: ItemTemplateType<Crate.Template> = Crate.Template

    /**
     * 组件: [CustomName]
     */
    val CUSTOM_NAME: ItemTemplateType<CustomName.Template> = CustomName.Template

    /**
     * 组件: [Damageable]
     */
    val DAMAGEABLE: ItemTemplateType<Damageable.Template> = Damageable.Template

    /**
     * 组件: [ItemElements]
     */
    val ELEMENTS: ItemTemplateType<ItemElements.Template> = ItemElements.Template

    /**
     * 组件: [FireResistant]
     */
    val FIRE_RESISTANT: ItemTemplateType<FireResistant.Template> = FireResistant.Template

    /**
     * 组件: [FoodProperties]
     */
    val FOOD: ItemTemplateType<FoodProperties.Template> = FoodProperties.Template

    /**
     * 组件: [ItemName]
     */
    val ITEM_NAME: ItemTemplateType<ItemName.Template> = ItemName.Template

    /**
     * 组件: [ItemKizamiz]
     */
    val KIZAMIZ: ItemTemplateType<ItemKizamiz.Template> = ItemKizamiz.Template

    /**
     * 组件: [Kizamiable]
     */
    val KIZAMIABLE: ItemTemplateType<Kizamiable.Template> = Kizamiable.Template

    /**
     * 组件: [ItemLevel]
     */
    val LEVEL: ItemTemplateType<ItemLevel.Template> = ItemLevel.Template

    /**
     * 组件: [ExtraLore]
     */
    val LORE: ItemTemplateType<ExtraLore.Template> = ExtraLore.Template

    /**
     * 组件: [ItemRarity]
     */
    val RARITY: ItemTemplateType<ItemRarity.Template> = ItemRarity.Template

    /**
     * 组件: [Skillful]
     */
    val SKILLFUL: ItemTemplateType<Skillful.Template> = Skillful.Template

    /**
     * 组件: [Tool]
     */
    val TOOL: ItemTemplateType<Tool.Template> = Tool.Template

    /**
     * 组件: [ItemTracks]
     */
    val TRACKS: ItemTemplateType<ItemTracks.Template> = ItemTracks.Template

    /**
     * 组件: [Unbreakable]
     */
    val UNBREAKABLE: ItemTemplateType<Unbreakable.Template> = Unbreakable.Template
}