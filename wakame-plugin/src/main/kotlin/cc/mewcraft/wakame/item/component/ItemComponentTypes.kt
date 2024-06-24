package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentHolder.Complex
import cc.mewcraft.wakame.item.component.ItemComponentHolder.Item
import cc.mewcraft.wakame.item.component.ItemComponentHolder.NBT
import cc.mewcraft.wakame.item.components.Arrow
import cc.mewcraft.wakame.item.components.Attributable
import cc.mewcraft.wakame.item.components.Crate
import cc.mewcraft.wakame.item.components.CustomModelData
import cc.mewcraft.wakame.item.components.CustomName
import cc.mewcraft.wakame.item.components.Damageable
import cc.mewcraft.wakame.item.components.FireResistant
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemKizamiz
import cc.mewcraft.wakame.item.components.ItemLore
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.ItemSkin
import cc.mewcraft.wakame.item.components.ItemSkinOwner
import cc.mewcraft.wakame.item.components.ItemStatistics
import cc.mewcraft.wakame.item.components.Tool
import cc.mewcraft.wakame.item.components.Unbreakable
import cc.mewcraft.wakame.registry.ItemComponentRegistry
import net.kyori.adventure.text.Component

object ItemComponentTypes {
    /**
     * 作为弹药.
     */
    // POC
    val ARROW: ItemComponentType.Valued<Arrow, NBT> = Arrow.Codec(ItemComponentConstants.ARROW).apply(::register)

    /**
     * 控制物品能否提供属性加成给玩家.
     */
    // POC
    val ATTRIBUTABLE: ItemComponentType.NonValued<NBT> = Attributable.Codec(ItemComponentConstants.ATTRIBUTABLE).apply(::register)

    /**
     * 物品的(所有)词条栏.
     */
    // POC
    val CELLS: ItemComponentType.Valued<ItemCells, NBT> = ItemCells.Codec(ItemComponentConstants.CELLS).apply(::register)

    /**
     * 作为盲盒.
     */
    val CRATE: ItemComponentType.Valued<Crate, NBT> = valued(ItemComponentConstants.CRATE)

    /**
     * 自定义模型数据. 对应原版组件: [`minecraft:custom_model_data`](https://minecraft.wiki/w/Data_component_format#custom_model_data)
     */
    val CUSTOM_MODEL_DATA: ItemComponentType.Valued<Int, Item> = CustomModelData.Codec(ItemComponentConstants.CUSTOM_MODEL_DATA).apply(::register)

    /**
     * 自定义名字. 对应原版组件: [`minecraft:custom_name`](https://minecraft.wiki/w/Data_component_format#custom_name)
     */
    // POC
    val CUSTOM_NAME: ItemComponentType.Valued<Component, Item> = CustomName.Codec(ItemComponentConstants.CUSTOM_NAME).apply(::register)

    /**
     * 损失的耐久.
     */
    val DAMAGE: ItemComponentType.Valued<Int, Item> = valued(ItemComponentConstants.DAMAGE)

    /**
     * 物品组件 [DAMAGE], [MAX_DAMAGE], [UNBREAKABLE] 的整合.
     */
    // POC
    val DAMAGEABLE: ItemComponentType.Valued<Damageable, Complex> = Damageable.Codec(ItemComponentConstants.DAMAGEABLE).apply(::register)

    /**
     * 物品元素.
     */
    // POC
    val ELEMENTS: ItemComponentType.Valued<ItemElements, NBT> = ItemElements.Codec(ItemComponentConstants.ELEMENTS).apply(::register)

    /**
     * 免疫火焰伤害. 对应原版组件: [`minecraft:fire_resistant`](https://minecraft.wiki/w/Data_component_format#fire_resistant)
     */
    // POC
    val FIRE_RESISTANT: ItemComponentType.NonValued<Item> = FireResistant.Codec(ItemComponentConstants.FIRE_RESISTANT).apply(::register)

    /**
     * 作为食物.
     */
    // POC
    val FOOD: ItemComponentType.Valued<FoodProperties, Complex> = FoodProperties.Codec(ItemComponentConstants.FOOD).apply(::register)

    /**
     * 物品名字. 对应原版组件: [`minecraft:item_name`](https://minecraft.wiki/w/Data_component_format#item_name)
     */
    val ITEM_NAME: ItemComponentType.Valued<Component, Item> = valued(ItemComponentConstants.ITEM_NAME)

    /**
     * 物品铭刻.
     */
    val KIZAMIZ: ItemComponentType.Valued<ItemKizamiz, NBT> = valued(ItemComponentConstants.KIZAMIZ)

    /**
     * 控制物品能否提供铭刻加成给玩家.
     */
    val KIZAMIABLE: ItemComponentType.NonValued<NBT> = nonValuedByNbt(ItemComponentConstants.KIZAMIABLE)

    /**
     * 物品等级.
     */
    val LEVEL: ItemComponentType.Valued<Int, NBT> = valued(ItemComponentConstants.LEVEL)

    /**
     * 物品描述 (不同于原版物品组件 `minecraft:lore`).
     */
    val LORE: ItemComponentType.Valued<ItemLore, NBT> = valued(ItemComponentConstants.LORE)

    /**
     * 最大可损失的耐久.
     */
    val MAX_DAMAGE: ItemComponentType.Valued<Int, Item> = valued(ItemComponentConstants.MAX_DAMAGE)

    /**
     * 物品稀有度.
     */
    val RARITY: ItemComponentType.Valued<ItemRarity, NBT> = valued(ItemComponentConstants.RARITY)

    /**
     * 控制物品能否提供技能加成给玩家.
     */
    val SKILLFUL: ItemComponentType.NonValued<NBT> = nonValuedByNbt(ItemComponentConstants.SKILLFUL)

    /**
     * 物品皮肤.
     */
    val SKIN: ItemComponentType.Valued<ItemSkin, NBT> = valued(ItemComponentConstants.SKIN)

    /**
     * 物品皮肤所有者.
     */
    val SKIN_OWNER: ItemComponentType.Valued<ItemSkinOwner, NBT> = valued(ItemComponentConstants.SKIN_OWNER)

    /**
     * 物品的(所有)统计数据.
     */
    val STATISTICS: ItemComponentType.Valued<ItemStatistics, NBT> = valued(ItemComponentConstants.STATISTICS)

    /**
     * 作为工具.
     */
    val TOOL: ItemComponentType.Valued<Tool, Item> = valued(ItemComponentConstants.TOOL)

    /**
     * 不损失耐久度.
     */
    // POC
    val UNBREAKABLE: ItemComponentType.Valued<Unbreakable, Item> = Unbreakable.Codec(ItemComponentConstants.UNBREAKABLE).apply(::register)

    private fun register(type: ItemComponentType) {
        ItemComponentRegistry.TYPES.register(type.id, type)
    }

    private fun <T, S : ItemComponentHolder> valued(id: String): ItemComponentType.Valued<T, S> {
        TODO()
    }

    private fun <S : ItemComponentHolder> nonValued(id: String): ItemComponentType.NonValued<S> {
        TODO()
    }

    // FIXME 如果 Type 需要负责返回一个组件的提示框文本,
    //  那么 Type 的成员就需要能够直接访问组件的数据.
    //  然而这跟当前的架构完全不同 - 因为 Type 实际是一个单例.
    //  这也就意味着组件的提示框文本必须由 Value 来提供.
    //  解决办法: Codec 产生 Value 产生 Name/LoreLine.
    //  Value 如果是 NonValued 就定义为单例 (kt object)
    /**
     * 创建一个 [ItemComponentType.NonValued] 实例, 其数据储存在 [NBT].
     */
    private fun nonValuedByNbt(id: String): ItemComponentType.NonValued<NBT> {
        return object : ItemComponentType.NonValued<NBT> {
            override val id: String = id
            override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT
            override fun read(holder: NBT): Boolean = false
            override fun write(holder: NBT, value: Boolean) = Unit
            override fun remove(holder: NBT) = Unit
        }
    }
}