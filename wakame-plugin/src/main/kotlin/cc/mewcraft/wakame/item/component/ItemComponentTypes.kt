package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.registry.ItemComponentRegistry

/**
 * 物品组件(wakame)的所有类型.
 */
object ItemComponentTypes {
    /**
     * 将物品作为箭矢.
     */
    val ARROW: ItemComponentType<Unit> = ItemArrow.codec(ItemConstants.ARROW).register()

    /**
     * 物品的攻速
     */
    val ATTACK_SPEED: ItemComponentType<ItemAttackSpeed> = ItemAttackSpeed.codec(ItemConstants.ATTACK_SPEED).register()

    /**
     * 控制物品能否提供属性加成给玩家.
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val ATTRIBUTABLE: ItemComponentType<Attributable> = Attributable.codec(ItemConstants.ATTRIBUTABLE).register()

    /**
     * 物品的属性修饰符.
     *
     * 对应原版组件: [`minecraft:attribute_modifiers`](https://minecraft.wiki/w/Data_component_format#attribute_modifiers)
     */
    val ATTRIBUTE_MODIFIERS: ItemComponentType<ItemAttributeModifiers> = ItemAttributeModifiers.codec(ItemConstants.ATTRIBUTE_MODIFIERS).register()

    /**
     * 将物品作为弓.
     */
    val BOW: ItemComponentType<Unit> = ItemBow.codec(ItemConstants.BOW).register()

    /**
     * 冒险模式的玩家使用此物品可以破坏的方块.
     *
     * 对应原版组件: [`minecraft:can_break`](https://minecraft.wiki/w/Data_component_format#can_break)
     */
    val CAN_BREAK: ItemComponentType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_BREAK).register()

    /**
     * 冒险模式的玩家可以使用此物品与指定方块进行交互.
     *
     * 对应原版组件: [`minecraft:can_place_on`](https://minecraft.wiki/w/Data_component_format#can_place_on)
     */
    val CAN_PLACE_ON: ItemComponentType<ItemAdventurePredicate> = ItemAdventurePredicate.codec(ItemConstants.CAN_PLACE_ON).register()

    /**
     * 控制物品能否释放技能.
     */
    val CASTABLE: ItemComponentType<Castable> = Castable.codec(ItemConstants.CASTABLE).register()

    /**
     * 物品的(所有)词条栏.
     */
    val CELLS: ItemComponentType<ItemCells> = ItemCells.codec(ItemConstants.CELLS).register()

    /**
     * 将物品作为盲盒.
     */
    val CRATE: ItemComponentType<ItemCrate> = ItemCrate.codec(ItemConstants.CRATE).register()

    /**
     * 自定义模型数据.
     *
     * 对应原版组件: [`minecraft:custom_model_data`](https://minecraft.wiki/w/Data_component_format#custom_model_data)
     */
    val CUSTOM_MODEL_DATA: ItemComponentType<CustomModelData> = CustomModelData.codec(ItemConstants.CUSTOM_MODEL_DATA).register()

    /**
     * 自定义名字.
     *
     * 对应原版组件: [`minecraft:custom_name`](https://minecraft.wiki/w/Data_component_format#custom_name)
     */
    val CUSTOM_NAME: ItemComponentType<CustomName> = CustomName.codec(ItemConstants.CUSTOM_NAME).register()

    /**
     * 物品已经损失的耐久.
     *
     * 对应原版组件: [`minecraft:damage`](https://minecraft.wiki/w/Data_component_format#damage)
     */
    val DAMAGE: ItemComponentType<Int> = ItemDamage.codec(ItemConstants.DAMAGE).register()

    /**
     * 物品组件 [DAMAGE], [MAX_DAMAGE], [UNBREAKABLE] 的整合.
     */
    val DAMAGEABLE: ItemComponentType<Damageable> = Damageable.codec(ItemConstants.DAMAGEABLE).register()

    /**
     * 皮革的颜色.
     *
     * 对应原版组件: [`minecraft:dyed_color`](https://minecraft.wiki/w/Data_component_format#dyed_color)
     */
    val DYED_COLOR: ItemComponentType<ItemDyeColor> = ItemDyeColor.codec(ItemConstants.DYED_COLOR).register()

    /**
     * 物品的元素.
     */
    val ELEMENTS: ItemComponentType<ItemElements> = ItemElements.codec(ItemConstants.ELEMENTS).register()

    /**
     * 物品的附魔.
     *
     * 对应原版组件: [`minecraft:enchantments`](https://minecraft.wiki/w/Data_component_format#enchantments)
     */
    val ENCHANTMENTS: ItemComponentType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.ENCHANTMENTS).register()

    /**
     * 拥有此组件将使物品免疫火焰伤害.
     *
     * 对应原版组件: [`minecraft:fire_resistant`](https://minecraft.wiki/w/Data_component_format#fire_resistant)
     */
    val FIRE_RESISTANT: ItemComponentType<FireResistant> = FireResistant.codec(ItemConstants.FIRE_RESISTANT).register()

    /**
     * 将物品作为食物.
     *
     * 对应原版组件: [`minecraft:food`](https://minecraft.wiki/w/Data_component_format#food)
     */
    val FOOD: ItemComponentType<FoodProperties> = FoodProperties.codec(ItemConstants.FOOD).register()

    /**
     * 可发光的物品
     */
    val GLOWABLE: ItemComponentType<ItemGlowable> = ItemGlowable.codec(ItemConstants.GLOWABLE).register()

    /**
     * 隐藏提示框.
     *
     * 对应原版组件: [`minecraft:hide_tooltip`](https://minecraft.wiki/w/Data_component_format#hide_tooltip)
     */
    val HIDE_TOOLTIP: ItemComponentType<HideTooltip> = HideTooltip.codec(ItemConstants.HIDE_TOOLTIP).register()

    /**
     * 隐藏额外的提示框.
     *
     * 对应原版组件: [`minecraft:hide_additional_tooltip`](https://minecraft.wiki/w/Data_component_format#hide_additional_tooltip)
     */
    val HIDE_ADDITIONAL_TOOLTIP: ItemComponentType<HideAdditionalTooltip> = HideAdditionalTooltip.codec(ItemConstants.HIDE_ADDITIONAL_TOOLTIP).register()

    /**
     * 物品名字.
     *
     * 对应原版组件: [`minecraft:item_name`](https://minecraft.wiki/w/Data_component_format#item_name)
     */
    val ITEM_NAME: ItemComponentType<ItemName> = ItemName.codec(ItemConstants.ITEM_NAME).register()

    /**
     * 物品的铭刻.
     */
    val KIZAMIZ: ItemComponentType<ItemKizamiz> = ItemKizamiz.codec(ItemConstants.KIZAMIZ).register()

    /**
     * 控制物品能否提供铭刻加成给玩家.
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val KIZAMIABLE: ItemComponentType<Kizamiable> = Kizamiable.codec(ItemConstants.KIZAMIABLE).register()

    /**
     * 物品的等级.
     */
    val LEVEL: ItemComponentType<ItemLevel> = ItemLevel.codec(ItemConstants.LEVEL).register()

    /**
     * 物品的描述 (不同于原版物品组件 `minecraft:lore`).
     */
    val LORE: ItemComponentType<ExtraLore> = ExtraLore.codec(ItemConstants.LORE).register()

    /**
     * 物品最大可损失的耐久.
     *
     * 对应原版组件: [`minecraft:max_damage`](https://minecraft.wiki/w/Data_component_format#max_damage)
     */
    val MAX_DAMAGE: ItemComponentType<Int> = ItemMaxDamage.codec(ItemConstants.MAX_DAMAGE).register()

    /**
     * 将物品作为便携式核心, 用于重铸系统.
     */
    val PORTABLE_CORE: ItemComponentType<PortableCore> = PortableCore.codec(ItemConstants.PORTABLE_CORE).register()

    /**
     * 物品的稀有度.
     */
    val RARITY: ItemComponentType<ItemRarity> = ItemRarity.codec(ItemConstants.RARITY).register()

    /**
     * 控制物品能否提供技能加成给玩家.
     */
    @Deprecated("与 ItemSlot 有高度重合")
    val SKILLFUL: ItemComponentType<Skillful> = Skillful.codec(ItemConstants.SKILLFUL).register()

    /**
     * 物品的皮肤.
     */
    val SKIN: ItemComponentType<ItemSkin> = dummy<ItemSkin>(ItemConstants.SKIN).register()

    /**
     * 物品的皮肤的所有者.
     */
    val SKIN_OWNER: ItemComponentType<ItemSkinOwner> = dummy<ItemSkinOwner>(ItemConstants.SKIN_OWNER).register()

    /**
     * 附魔书内存储的魔咒.
     *
     * 对应原版组件: [`minecraft:stored_enchantments`](https://minecraft.wiki/w/Data_component_format#stored_enchantments)
     */
    val STORED_ENCHANTMENTS: ItemComponentType<ItemEnchantments> = ItemEnchantments.codec(ItemConstants.STORED_ENCHANTMENTS).register()

    /**
     * 将物品作为系统物品. 系统物品的机制:
     * - 玩家不允许获取和使用
     * - 不会被物品发包系统修改
     * - 专门用于, 例如GUI容器内的物品
     *
     * 这也意味着系统物品的提示框文本完全取决于之上的原版组件.
     */
    val SYSTEM_USE: ItemComponentType<Unit> = SystemUse.codec(ItemConstants.SYSTEM_USE).register()

    /**
     * 将物品作为工具.
     *
     * 对应原版组件: [`minecraft:tool`](https://minecraft.wiki/w/Data_component_format#tool)
     */
    val TOOL: ItemComponentType<Tool> = Tool.codec(ItemConstants.TOOL).register()

    /**
     * 记录了物品的统计数据. 如果拥有此组件, 各种信息将被记录到物品之上.
     */
    val TRACKS: ItemComponentType<ItemTracks> = ItemTracks.codec(ItemConstants.TRACKABLE).register()

    /**
     * 盔甲纹饰.
     *
     * 对应原版组件: [`minecraft:trim`](https://minecraft.wiki/w/Data_component_format#trim)
     */
    val TRIM: ItemComponentType<ArmorTrim> = ArmorTrim.codec(ItemConstants.TRIM).register()

    /**
     * 拥有此组件将阻止物品损失耐久度.
     *
     * 对应原版组件: [`minecraft:unbreakable`](https://minecraft.wiki/w/Data_component_format#unbreakable)
     */
    val UNBREAKABLE: ItemComponentType<Unbreakable> = Unbreakable.codec(ItemConstants.UNBREAKABLE).register()

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