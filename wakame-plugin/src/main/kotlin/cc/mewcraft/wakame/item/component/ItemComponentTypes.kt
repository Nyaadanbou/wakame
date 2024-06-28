package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.components.Arrow
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Castable
import cc.mewcraft.wakame.item.components.Crate
import cc.mewcraft.wakame.item.components.CustomModelData
import cc.mewcraft.wakame.item.components.CustomName
import cc.mewcraft.wakame.item.components.Damageable
import cc.mewcraft.wakame.item.components.ExtraLore
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemDamage
import cc.mewcraft.wakame.item.components.ItemElements
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

object ItemComponentTypes {
    /**
     * 将物品作为弹药.
     */
    // POC
    val ARROW: ItemComponentType<Arrow, ItemComponentHolder.NBT> = Arrow.Codec(ItemComponentConstants.ARROW).apply(::register)

    /**
     * 控制物品能否提供属性加成给玩家.
     */
    // POC
    val ATTRIBUTABLE: ItemComponentType<Attributable, ItemComponentHolder.NBT> = Attributable.Codec(ItemComponentConstants.ATTRIBUTABLE).apply(::register)

    /**
     * 控制物品能否释放技能.
     */
    // POC
    val CASTABLE: ItemComponentType<Castable, ItemComponentHolder.NBT> = Castable.Codec(ItemComponentConstants.CASTABLE).apply(::register)

    /**
     * 物品的词条栏.
     */
    // POC
    val CELLS: ItemComponentType<ItemCells, ItemComponentHolder.NBT> = ItemCells.Codec(ItemComponentConstants.CELLS).apply(::register)

    /**
     * 将物品作为盲盒.
     */
    val CRATE: ItemComponentType<Crate, ItemComponentHolder.NBT> = Crate.Codec(ItemComponentConstants.CRATE)

    /**
     * 自定义模型数据. 对应原版组件: [`minecraft:custom_model_data`](https://minecraft.wiki/w/Data_component_format#custom_model_data)
     */
    // POC
    val CUSTOM_MODEL_DATA: ItemComponentType<Int, ItemComponentHolder.Item> = CustomModelData.Codec(ItemComponentConstants.CUSTOM_MODEL_DATA).apply(::register)

    /**
     * 自定义名字. 对应原版组件: [`minecraft:custom_name`](https://minecraft.wiki/w/Data_component_format#custom_name)
     */
    // POC
    val CUSTOM_NAME: ItemComponentType<CustomName, ItemComponentHolder.NBT> = CustomName.Codec(ItemComponentConstants.CUSTOM_NAME).apply(::register)

    /**
     * 物品已经损失的耐久.
     */
    val DAMAGE: ItemComponentType<Int, ItemComponentHolder.Item> = ItemDamage.Codec(ItemComponentConstants.DAMAGE)

    /**
     * 物品组件 [DAMAGE], [MAX_DAMAGE], [UNBREAKABLE] 的整合.
     */
    // POC
    val DAMAGEABLE: ItemComponentType<Damageable, ItemComponentHolder.Complex> = Damageable.Codec(ItemComponentConstants.DAMAGEABLE).apply(::register)

    /**
     * 物品的元素.
     */
    // POC
    val ELEMENTS: ItemComponentType<ItemElements, ItemComponentHolder.NBT> = ItemElements.Codec(ItemComponentConstants.ELEMENTS).apply(::register)

    /**
     * 拥有此组件将使物品免疫火焰伤害. 对应原版组件: [`minecraft:fire_resistant`](https://minecraft.wiki/w/Data_component_format#fire_resistant)
     */
    // POC
    val FIRE_RESISTANT: ItemComponentType<FireResistant, ItemComponentHolder.Item> = FireResistant.Codec(ItemComponentConstants.FIRE_RESISTANT).apply(::register)

    /**
     * 将物品作为食物.
     */
    // POC
    val FOOD: ItemComponentType<FoodProperties, ItemComponentHolder.Complex> = FoodProperties.Codec(ItemComponentConstants.FOOD).apply(::register)

    /**
     * 物品名字. 对应原版组件: [`minecraft:item_name`](https://minecraft.wiki/w/Data_component_format#item_name)
     */
    val ITEM_NAME: ItemComponentType<ItemName, ItemComponentHolder.NBT> = ItemName.Codec(ItemComponentConstants.ITEM_NAME)

    /**
     * 物品的铭刻.
     */
    // POC
    val KIZAMIZ: ItemComponentType<ItemKizamiz, ItemComponentHolder.NBT> = ItemKizamiz.Codec(ItemComponentConstants.KIZAMIZ).apply(::register)

    /**
     * 控制物品能否提供铭刻加成给玩家.
     */
    // POC
    val KIZAMIABLE: ItemComponentType<Kizamiable, ItemComponentHolder.NBT> = Kizamiable.Codec(ItemComponentConstants.KIZAMIABLE).apply(::register)

    /**
     * 物品的等级.
     */
    // POC
    val LEVEL: ItemComponentType<ItemLevel, ItemComponentHolder.NBT> = ItemLevel.Codec(ItemComponentConstants.LEVEL).apply(::register)

    /**
     * 物品的描述 (不同于原版物品组件 `minecraft:lore`).
     */
    // POC
    val LORE: ItemComponentType<ExtraLore, ItemComponentHolder.NBT> = ExtraLore.Codec(ItemComponentConstants.LORE).apply(::register)

    /**
     * 物品最大可损失的耐久.
     */
    val MAX_DAMAGE: ItemComponentType<Int, ItemComponentHolder.Item> = ItemMaxDamage.Codec(ItemComponentConstants.MAX_DAMAGE)

    /**
     * 物品的稀有度.
     */
    // POC
    val RARITY: ItemComponentType<ItemRarity, ItemComponentHolder.NBT> = ItemRarity.Codec(ItemComponentConstants.RARITY).apply(::register)

    /**
     * 控制物品能否提供技能加成给玩家.
     */
    // POC
    val SKILLFUL: ItemComponentType<Skillful, ItemComponentHolder.NBT> = Skillful.Codec(ItemComponentConstants.SKILLFUL).apply(::register)

    /**
     * 物品的皮肤.
     */
    val SKIN: ItemComponentType<ItemSkin, ItemComponentHolder.NBT> = dummy(ItemComponentConstants.SKIN)

    /**
     * 物品的皮肤的所有者.
     */
    val SKIN_OWNER: ItemComponentType<ItemSkinOwner, ItemComponentHolder.NBT> = dummy(ItemComponentConstants.SKIN_OWNER)

    /**
     * 将物品作为系统物品. 系统物品的机制:
     * - 玩家不允许获取和使用
     * - 不会被物品发包系统修改
     * - 专门用于, 例如GUI容器内的物品
     *
     * 这也意味着系统物品的提示框文本完全取决于之上的原版组件.
     */
    // POC
    val SYSTEM_USE: ItemComponentType<Unit, ItemComponentHolder.NBT> = SystemUse.Codec(ItemComponentConstants.SYSTEM_USE)

    /**
     * 将物品作为工具.
     */
    // POC
    val TOOL: ItemComponentType<Tool, ItemComponentHolder.Item> = Tool.Codec(ItemComponentConstants.TOOL).apply(::register)

    /**
     * 记录了物品的统计数据. 如果拥有此组件, 各种信息将被记录到物品之上.
     */
    // POC
    val TRACKS: ItemComponentType<ItemTracks, ItemComponentHolder.NBT> = ItemTracks.Codec(ItemComponentConstants.TRACKABLE).apply(::register)

    /**
     * 拥有此组件将阻止物品损失耐久度.
     */
    // POC
    val UNBREAKABLE: ItemComponentType<Unbreakable, ItemComponentHolder.Item> = Unbreakable.Codec(ItemComponentConstants.UNBREAKABLE).apply(::register)

    private fun <T, S : ItemComponentHolder> register(type: ItemComponentType<T, S>) {
        ItemComponentRegistry.TYPES.register(type.id, type)
    }

    private fun <T, S : ItemComponentHolder> dummy(id: String): ItemComponentType<T, S> {
        return object : ItemComponentType<T, S> {
            override val id: String = id
            override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT
            override fun remove(holder: S) = Unit
            override fun write(holder: S, value: T) = Unit
            override fun read(holder: S): T? = null
        }
    }

    // FIXME 如果 Type 需要负责返回一个组件的提示框文本,
    //  那么 Type 的成员就需要能够直接访问组件的数据.
    //  然而这跟当前的架构完全不同 - 因为 Type 实际是一个单例.
    //  这也就意味着组件的提示框文本必须由 Value 来提供.
    //  解决办法: Codec 产生 Value 产生 Name/LoreLine.
    //  Value 如果是 NonValued 就定义为单例 (kt object)
}