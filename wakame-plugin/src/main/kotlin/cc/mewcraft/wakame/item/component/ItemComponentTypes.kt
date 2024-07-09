package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.components.ArmorTrim
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Castable
import cc.mewcraft.wakame.item.components.CustomModelData
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
import cc.mewcraft.wakame.item.components.Kizamiable
import cc.mewcraft.wakame.item.components.Skillful
import cc.mewcraft.wakame.item.components.SystemUse
import cc.mewcraft.wakame.item.components.Tool
import cc.mewcraft.wakame.item.components.Unbreakable
import cc.mewcraft.wakame.registry.ItemComponentRegistry

/**
 * 物品组件(wakame)的所有类型.
 */
object ItemComponentTypes {
    /**
     * 将物品作为弹药.
     */
    val ARROW: ItemComponentType<ItemArrow> = ItemArrow.codec(ItemComponentConstants.ARROW).register()

    /**
     * 控制物品能否提供属性加成给玩家.
     */
    val ATTRIBUTABLE: ItemComponentType<Attributable> = Attributable.codec(ItemComponentConstants.ATTRIBUTABLE).register()

    /**
     * 物品的属性修饰符.
     *
     * 对应原版组件: [`minecraft:attribute_modifiers`](https://minecraft.wiki/w/Data_component_format#attribute_modifiers)
     */
    val ATTRIBUTE_MODIFIERS: ItemComponentType<ItemAttributeModifiers> = ItemAttributeModifiers.codec(ItemComponentConstants.ATTRIBUTE_MODIFIERS).register()

    /**
     * 冒险模式的玩家使用此物品可以破坏的方块.
     *
     * 对应原版组件: [`minecraft:can_break`](https://minecraft.wiki/w/Data_component_format#can_break)
     */
    val CAN_BREAK: ItemComponentType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemComponentConstants.CAN_BREAK).register()

    /**
     * 冒险模式的玩家可以使用此物品与指定方块进行交互.
     *
     * 对应原版组件: [`minecraft:can_place_on`](https://minecraft.wiki/w/Data_component_format#can_place_on)
     */
    val CAN_PLACE_ON: ItemComponentType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemComponentConstants.CAN_PLACE_ON).register()

    /**
     * 控制物品能否释放技能.
     */
    val CASTABLE: ItemComponentType<Castable> = Castable.codec(ItemComponentConstants.CASTABLE).register()

    /**
     * 物品的(所有)词条栏.
     */
    val CELLS: ItemComponentType<ItemCells> = ItemCells.codec(ItemComponentConstants.CELLS).register()

    /**
     * 将物品作为盲盒.
     */
    val CRATE: ItemComponentType<ItemCrate> = ItemCrate.codec(ItemComponentConstants.CRATE).register()

    /**
     * 自定义模型数据.
     *
     * 对应原版组件: [`minecraft:custom_model_data`](https://minecraft.wiki/w/Data_component_format#custom_model_data)
     */
    val CUSTOM_MODEL_DATA: ItemComponentType<CustomModelData> = CustomModelData.codec(ItemComponentConstants.CUSTOM_MODEL_DATA).register()

    /**
     * 自定义名字.
     *
     * 对应原版组件: [`minecraft:custom_name`](https://minecraft.wiki/w/Data_component_format#custom_name)
     */
    val CUSTOM_NAME: ItemComponentType<CustomName> = CustomName.codec(ItemComponentConstants.CUSTOM_NAME).register()

    /**
     * 物品已经损失的耐久.
     *
     * 对应原版组件: [`minecraft:damage`](https://minecraft.wiki/w/Data_component_format#damage)
     */
    val DAMAGE: ItemComponentType<Int> = ItemDamage.codec(ItemComponentConstants.DAMAGE).register()

    /**
     * 物品组件 [DAMAGE], [MAX_DAMAGE], [UNBREAKABLE] 的整合.
     */
    val DAMAGEABLE: ItemComponentType<Damageable> = Damageable.codec(ItemComponentConstants.DAMAGEABLE).register()

    /**
     * 皮革的颜色.
     *
     * 对应原版组件: [`minecraft:dyed_color`](https://minecraft.wiki/w/Data_component_format#dyed_color)
     */
    val DYED_COLOR: ItemComponentType<ItemDyeColor> = ItemDyeColor.codec(ItemComponentConstants.DYED_COLOR).register()

    /**
     * 物品的元素.
     */
    val ELEMENTS: ItemComponentType<ItemElements> = ItemElements.codec(ItemComponentConstants.ELEMENTS).register()

    /**
     * 物品的附魔.
     *
     * 对应原版组件: [`minecraft:enchantments`](https://minecraft.wiki/w/Data_component_format#enchantments)
     */
    val ENCHANTMENTS: ItemComponentType<ItemEnchantments> = ItemEnchantments.codec(ItemComponentConstants.ENCHANTMENTS).register()

    /**
     * 拥有此组件将使物品免疫火焰伤害.
     *
     * 对应原版组件: [`minecraft:fire_resistant`](https://minecraft.wiki/w/Data_component_format#fire_resistant)
     */
    val FIRE_RESISTANT: ItemComponentType<FireResistant> = FireResistant.codec(ItemComponentConstants.FIRE_RESISTANT).register()

    /**
     * 将物品作为食物.
     *
     * 对应原版组件: [`minecraft:food`](https://minecraft.wiki/w/Data_component_format#food)
     */
    val FOOD: ItemComponentType<FoodProperties> = FoodProperties.codec(ItemComponentConstants.FOOD).register()

    /**
     * 隐藏提示框.
     *
     * 对应原版组件: [`minecraft:hide_tooltip`](https://minecraft.wiki/w/Data_component_format#hide_tooltip)
     */
    val HIDE_TOOLTIP: ItemComponentType<HideTooltip> = HideTooltip.codec(ItemComponentConstants.HIDE_TOOLTIP).register()

    /**
     * 隐藏额外的提示框.
     *
     * 对应原版组件: [`minecraft:hide_additional_tooltip`](https://minecraft.wiki/w/Data_component_format#hide_additional_tooltip)
     */
    val HIDE_ADDITIONAL_TOOLTIP: ItemComponentType<HideAdditionalTooltip> = HideAdditionalTooltip.codec(ItemComponentConstants.HIDE_ADDITIONAL_TOOLTIP).register()

    /**
     * 物品名字.
     *
     * 对应原版组件: [`minecraft:item_name`](https://minecraft.wiki/w/Data_component_format#item_name)
     */
    val ITEM_NAME: ItemComponentType<ItemName> = ItemName.codec(ItemComponentConstants.ITEM_NAME).register()

    /**
     * 物品的铭刻.
     */
    val KIZAMIZ: ItemComponentType<ItemKizamiz> = ItemKizamiz.codec(ItemComponentConstants.KIZAMIZ).register()

    /**
     * 控制物品能否提供铭刻加成给玩家.
     */
    val KIZAMIABLE: ItemComponentType<Kizamiable> = Kizamiable.codec(ItemComponentConstants.KIZAMIABLE).register()

    /**
     * 物品的等级.
     */
    val LEVEL: ItemComponentType<ItemLevel> = ItemLevel.codec(ItemComponentConstants.LEVEL).register()

    /**
     * 物品的描述 (不同于原版物品组件 `minecraft:lore`).
     */
    val LORE: ItemComponentType<ExtraLore> = ExtraLore.codec(ItemComponentConstants.LORE).register()

    /**
     * 物品最大可损失的耐久.
     *
     * 对应原版组件: [`minecraft:max_damage`](https://minecraft.wiki/w/Data_component_format#max_damage)
     */
    val MAX_DAMAGE: ItemComponentType<Int> = ItemMaxDamage.codec(ItemComponentConstants.MAX_DAMAGE).register()

    /**
     * 物品的稀有度.
     */
    val RARITY: ItemComponentType<ItemRarity> = ItemRarity.codec(ItemComponentConstants.RARITY).register()

    /**
     * 控制物品能否提供技能加成给玩家.
     */
    val SKILLFUL: ItemComponentType<Skillful> = Skillful.codec(ItemComponentConstants.SKILLFUL).register()

    /**
     * 物品的皮肤.
     */
    val SKIN: ItemComponentType<ItemSkin> = dummy<ItemSkin>(ItemComponentConstants.SKIN).register()

    /**
     * 物品的皮肤的所有者.
     */
    val SKIN_OWNER: ItemComponentType<ItemSkinOwner> = dummy<ItemSkinOwner>(ItemComponentConstants.SKIN_OWNER).register()

    /**
     * 附魔书内存储的魔咒.
     *
     * 对应原版组件: [`minecraft:stored_enchantments`](https://minecraft.wiki/w/Data_component_format#stored_enchantments)
     */
    val STORED_ENCHANTMENTS: ItemComponentType<ItemEnchantments> = ItemEnchantments.codec(ItemComponentConstants.STORED_ENCHANTMENTS).register()

    /**
     * 将物品作为系统物品. 系统物品的机制:
     * - 玩家不允许获取和使用
     * - 不会被物品发包系统修改
     * - 专门用于, 例如GUI容器内的物品
     *
     * 这也意味着系统物品的提示框文本完全取决于之上的原版组件.
     */
    val SYSTEM_USE: ItemComponentType<Unit> = SystemUse.codec(ItemComponentConstants.SYSTEM_USE).register()

    /**
     * 将物品作为工具.
     *
     * 对应原版组件: [`minecraft:tool`](https://minecraft.wiki/w/Data_component_format#tool)
     */
    val TOOL: ItemComponentType<Tool> = Tool.codec(ItemComponentConstants.TOOL).register()

    /**
     * 记录了物品的统计数据. 如果拥有此组件, 各种信息将被记录到物品之上.
     */
    val TRACKS: ItemComponentType<ItemTracks> = ItemTracks.codec(ItemComponentConstants.TRACKABLE).register()

    /**
     * 盔甲纹饰.
     *
     * 对应原版组件: [`minecraft:trim`](https://minecraft.wiki/w/Data_component_format#trim)
     */
    val TRIM: ItemComponentType<ArmorTrim> = ArmorTrim.codec(ItemComponentConstants.TRIM).register()

    /**
     * 拥有此组件将阻止物品损失耐久度.
     *
     * 对应原版组件: [`minecraft:unbreakable`](https://minecraft.wiki/w/Data_component_format#unbreakable)
     */
    val UNBREAKABLE: ItemComponentType<Unbreakable> = Unbreakable.codec(ItemComponentConstants.UNBREAKABLE).register()

    /**
     * 注册该组件类型.
     *
     * 注意事项: 每个组件都需要注册, 不然有些功能会无法使用.
     * 具体是哪些功能, 看看这个 Registry 被哪些代码调用过.
     */
    private fun <T> ItemComponentType<T>.register(): ItemComponentType<T> {
        ItemComponentRegistry.TYPES.register(this.id, this)
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