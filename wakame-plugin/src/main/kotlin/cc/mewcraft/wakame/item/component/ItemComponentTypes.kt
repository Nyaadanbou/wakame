package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.components.ArmorTrim
import cc.mewcraft.wakame.item.components.CustomName
import cc.mewcraft.wakame.item.components.DamageResistant
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.HideAdditionalTooltip
import cc.mewcraft.wakame.item.components.HideTooltip
import cc.mewcraft.wakame.item.components.ItemAdventurePredicate
import cc.mewcraft.wakame.item.components.ItemAttackSpeed
import cc.mewcraft.wakame.item.components.ItemAttributeModifiers
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemCrate
import cc.mewcraft.wakame.item.components.ItemDamage
import cc.mewcraft.wakame.item.components.ItemDyeColor
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemEnchantments
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemMaxDamage
import cc.mewcraft.wakame.item.components.ItemName
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.ItemSkin
import cc.mewcraft.wakame.item.components.ItemSkinOwner
import cc.mewcraft.wakame.item.components.ItemTracks
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.Tool
import cc.mewcraft.wakame.item.components.Unbreakable
import net.kyori.adventure.text.Component

/**
 * 本单例提供了物品组件的所有类型.
 */
internal object ItemComponentTypes {
    /**
     * 用于表示一个不存在的物品组件.
     */
    @JvmField
    val EMPTY: ItemComponentType<Nothing> = object : ItemComponentType<Nothing> {
        override val id: String = "empty"
        override fun read(holder: ItemComponentHolder): Nothing? = null
        override fun remove(holder: ItemComponentHolder) = Unit
        override fun write(holder: ItemComponentHolder, value: Nothing) = Unit
    }

    /**
     * 物品的攻击速度.
     */
    @JvmField
    val ATTACK_SPEED: ItemComponentType<ItemAttackSpeed> = ItemAttackSpeed.codec(ItemConstants.ATTACK_SPEED).register()

    /**
     * 物品的属性修饰符.
     *
     * 对应原版组件: [`minecraft:attribute_modifiers`](https://minecraft.wiki/w/Data_component_format#attribute_modifiers)
     */
    @JvmField
    val ATTRIBUTE_MODIFIERS: ItemComponentType<ItemAttributeModifiers> = ItemAttributeModifiers.codec(ItemConstants.ATTRIBUTE_MODIFIERS).register()

    /**
     * 冒险模式的玩家使用此物品可以破坏的方块.
     *
     * 对应原版组件: [`minecraft:can_break`](https://minecraft.wiki/w/Data_component_format#can_break)
     */
    @JvmField
    val CAN_BREAK: ItemComponentType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_BREAK).register()

    /**
     * 冒险模式的玩家可以使用此物品与指定方块进行交互.
     *
     * 对应原版组件: [`minecraft:can_place_on`](https://minecraft.wiki/w/Data_component_format#can_place_on)
     */
    @JvmField
    val CAN_PLACE_ON: ItemComponentType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_PLACE_ON).register()

    /**
     * 物品的(所有)核孔.
     */
    @JvmField
    val CELLS: ItemComponentType<ItemCells> = ItemCells.codec(ItemConstants.CELLS).register()

    /**
     * 将物品作为盲盒.
     */
    @JvmField
    val CRATE: ItemComponentType<ItemCrate> = ItemCrate.codec(ItemConstants.CRATE).register()

    /**
     * 自定义名字.
     *
     * 对应原版组件: [`minecraft:custom_name`](https://minecraft.wiki/w/Data_component_format#custom_name)
     */
    @JvmField
    val CUSTOM_NAME: ItemComponentType<Component> = CustomName.codec(ItemConstants.CUSTOM_NAME).register()

    /**
     * 物品已经损失的耐久.
     *
     * 对应原版组件: [`minecraft:damage`](https://minecraft.wiki/w/Data_component_format#damage)
     */
    @JvmField
    val DAMAGE: ItemComponentType<Int> = ItemDamage.codec(ItemConstants.DAMAGE).register()

    /**
     * 皮革的颜色.
     *
     * 对应原版组件: [`minecraft:dyed_color`](https://minecraft.wiki/w/Data_component_format#dyed_color)
     */
    @JvmField
    val DYED_COLOR: ItemComponentType<ItemDyeColor> = ItemDyeColor.codec(ItemConstants.DYED_COLOR).register()

    /**
     * 物品的元素.
     */
    @JvmField
    val ELEMENTS: ItemComponentType<ItemElements> = ItemElements.codec(ItemConstants.ELEMENTS).register()

    /**
     * 物品的附魔.
     *
     * 对应原版组件: [`minecraft:enchantments`](https://minecraft.wiki/w/Data_component_format#enchantments)
     */
    @JvmField
    val ENCHANTMENTS: ItemComponentType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.ENCHANTMENTS).register()

    /**
     * 拥有此组件将使物品免疫一些类型的伤害.
     *
     * 对应原版组件: [`minecraft:damage_resistant`](https://minecraft.wiki/w/Data_component_format#damage_resistant)
     */
    @JvmField
    val DAMAGE_RESISTANT: ItemComponentType<DamageResistant> = DamageResistant.codec(ItemConstants.DAMAGE_RESISTANT).register()

    /**
     * 将物品作为食物.
     *
     * 对应原版组件: [`minecraft:food`](https://minecraft.wiki/w/Data_component_format#food)
     */
    @JvmField
    val FOOD: ItemComponentType<FoodProperties> = FoodProperties.codec(ItemConstants.FOOD).register()

    /**
     * 隐藏提示框.
     *
     * 对应原版组件: [`minecraft:hide_tooltip`](https://minecraft.wiki/w/Data_component_format#hide_tooltip)
     */
    @JvmField
    val HIDE_TOOLTIP: ItemComponentType<HideTooltip> = HideTooltip.codec(ItemConstants.HIDE_TOOLTIP).register()

    /**
     * 隐藏额外的提示框.
     *
     * 对应原版组件: [`minecraft:hide_additional_tooltip`](https://minecraft.wiki/w/Data_component_format#hide_additional_tooltip)
     */
    @JvmField
    val HIDE_ADDITIONAL_TOOLTIP: ItemComponentType<HideAdditionalTooltip> = HideAdditionalTooltip.codec(ItemConstants.HIDE_ADDITIONAL_TOOLTIP).register()

    /**
     * 物品名字.
     *
     * 对应原版组件: [`minecraft:item_name`](https://minecraft.wiki/w/Data_component_format#item_name)
     */
    @JvmField
    val ITEM_NAME: ItemComponentType<Component> = ItemName.codec(ItemConstants.ITEM_NAME).register()

    /**
     * 物品的铭刻.
     */
    @JvmField
    val KIZAMIZ: ItemComponentType<ItemKizamiz> = ItemKizamiz.codec(ItemConstants.KIZAMIZ).register()

    /**
     * 物品的等级.
     */
    @JvmField
    val LEVEL: ItemComponentType<ItemLevel> = ItemLevel.codec(ItemConstants.LEVEL).register()

    /**
     * 物品最大可损失的耐久.
     *
     * 对应原版组件: [`minecraft:max_damage`](https://minecraft.wiki/w/Data_component_format#max_damage)
     */
    @JvmField
    val MAX_DAMAGE: ItemComponentType<Int> = ItemMaxDamage.codec(ItemConstants.MAX_DAMAGE).register()

    /**
     * 将物品作为便携式核心, 用于重铸系统.
     */
    @JvmField
    val PORTABLE_CORE: ItemComponentType<PortableCore> = PortableCore.codec(ItemConstants.PORTABLE_CORE).register()

    /**
     * 物品的稀有度.
     */
    @JvmField
    val RARITY: ItemComponentType<ItemRarity> = ItemRarity.codec(ItemConstants.RARITY).register()

    /**
     * 物品的重铸历史.
     */
    @JvmField
    val REFORGE_HISTORY: ItemComponentType<ReforgeHistory> = ReforgeHistory.codec(ItemConstants.REFORGE_HISTORY).register()

    /**
     * 物品的皮肤.
     */
    @JvmField
    val SKIN: ItemComponentType<ItemSkin> = dummy<ItemSkin>(ItemConstants.SKIN).register()

    /**
     * 物品的皮肤的所有者.
     */
    @JvmField
    val SKIN_OWNER: ItemComponentType<ItemSkinOwner> = dummy<ItemSkinOwner>(ItemConstants.SKIN_OWNER).register()

    /**
     * 附魔书内存储的魔咒.
     *
     * 对应原版组件: [`minecraft:stored_enchantments`](https://minecraft.wiki/w/Data_component_format#stored_enchantments)
     */
    @JvmField
    val STORED_ENCHANTMENTS: ItemComponentType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.STORED_ENCHANTMENTS).register()

    /**
     * 将物品作为工具.
     *
     * 对应原版组件: [`minecraft:tool`](https://minecraft.wiki/w/Data_component_format#tool)
     */
    @JvmField
    val TOOL: ItemComponentType<Tool> = Tool.codec(ItemConstants.TOOL).register()

    /**
     * 记录了物品的统计数据. 如果拥有此组件, 各种信息将被记录到物品之上.
     */
    @JvmField
    val TRACKS: ItemComponentType<ItemTracks> = ItemTracks.codec(ItemConstants.TRACKABLE).register()

    /**
     * 盔甲纹饰.
     *
     * 对应原版组件: [`minecraft:trim`](https://minecraft.wiki/w/Data_component_format#trim)
     */
    @JvmField
    val TRIM: ItemComponentType<ArmorTrim> = ArmorTrim.codec(ItemConstants.TRIM).register()

    /**
     * 拥有此组件将阻止物品损失耐久度.
     *
     * 对应原版组件: [`minecraft:unbreakable`](https://minecraft.wiki/w/Data_component_format#unbreakable)
     */
    @JvmField
    val UNBREAKABLE: ItemComponentType<Unbreakable> = Unbreakable.codec(ItemConstants.UNBREAKABLE).register()

    /**
     * 注册该组件类型.
     *
     * 注意事项: 每个组件都需要注册, 不然有些功能会无法使用.
     * 具体是哪些功能, 看看这个 Registry 被哪些代码调用过.
     */
    private fun <T> ItemComponentType<T>.register(): ItemComponentType<T> {
        ItemComponentRegistry.TYPES[this.id] = this
        return this
    }

    // 用来构建一个临时的组件类型; 临时的组件类型只是为了编译, 不应该被使用!
    private fun <T> dummy(id: String): ItemComponentType<T> {
        return object : ItemComponentType<T> {
            override val id: String = id
            override fun read(holder: ItemComponentHolder): T? = null
            override fun remove(holder: ItemComponentHolder) = Unit
            override fun write(holder: ItemComponentHolder, value: T) = Unit
        }
    }

    // 开发日记 2024/7/2
    // 如果 Type 需要负责返回一个组件的提示框文本,
    // 那么 Type 的成员就需要能够直接访问组件的数据.
    // 然而这跟当前的架构完全不同 - 因为 Type 实际是一个单例.
    // 这也就意味着组件的提示框文本必须由 Value 来提供.
    // 解决办法: Codec 产生 Value 产生 Name/LoreLine.
    // Value 如果是 NonValued 就定义为单例 (kt object)
}