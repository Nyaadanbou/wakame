package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext.Pos
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item2.ItemRef
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.item.fastLoreOrEmpty
import cc.mewcraft.wakame.util.item.itemNameOrType
import cc.mewcraft.wakame.util.require
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成站的一项输入要求.
 */
internal sealed interface RecipeChoice : Examinable {
    /**
     * 用于检查该 [RecipeChoice] 所要求的输入是否被满足.
     */
    val checker: ChoiceChecker<*>

    /**
     * 用于消耗该 [RecipeChoice] 所要求的输入 (从玩家身上).
     */
    val consumer: ChoiceConsumer<*>

    /**
     * 通过上下文检查此 [RecipeChoice] 的满足与否.
     * 上下文依靠 [ChoiceCheckerContextMap] 获取.
     */
    fun check(contextMap: ChoiceCheckerContextMap): Boolean

    /**
     * 将此 [RecipeChoice] 的消耗添加到上下文中.
     * 上下文依靠 [ChoiceConsumerContextMap] 获取.
     */
    fun consume(contextMap: ChoiceConsumerContextMap)

    /**
     * 获取此 [RecipeChoice] 展示用的物品堆叠.
     *
     * 由于合成输入不一定是固定的, 其展示用的物品堆叠与最终给予玩家的物品堆叠必须要有区别.
     * 原因是当一个物品基于某种规则随机生成时, 其展示用的物品堆叠应该向玩家表达清楚随机的规则,
     * 而不是直接展示众多随机结果中的一种. 这样可以使玩家对合成的随机结果有更清晰的认识.
     */
    fun displayItemStack(settings: BasicMenuSettings): ItemStack

    /**
     * [RecipeChoice] 的序列化器.
     */
    object Serializer : TypeSerializer2<RecipeChoice> {
        override fun deserialize(type: Type, node: ConfigurationNode): RecipeChoice {
            val choiceType = node.node("type").require<String>()
            when (choiceType) {
                ItemChoice.TYPE -> {
                    val item = node.node("id").require<ItemRef>()
                    val amount = node.node("amount").getInt(1)
                    require(amount > 0) { "Item amount must more than 0" }
                    return ItemChoice(item, amount)
                }

                ExpChoice.TYPE -> {
                    val amount = node.node("amount").require<Int>()
                    require(amount > 0) { "Exp amount must more than 0" }
                    return ExpChoice(amount)
                }

                else -> {
                    throw SerializationException("Unknown station choice type")
                }
            }
        }
    }
}


/* Internals */


/**
 * 物品类型的合成站输入.
 */
internal data class ItemChoice(
    val item: ItemRef,
    val amount: Int,
) : RecipeChoice {
    companion object {
        const val TYPE: String = "item"
    }

    override val checker: ItemChoiceChecker = ItemChoiceChecker
    override val consumer: ItemChoiceConsumer = ItemChoiceConsumer

    override fun check(contextMap: ChoiceCheckerContextMap): Boolean {
        val context = contextMap[checker]
        val inventory = context.inventorySnapshot

        if (!inventory.containsKey(item)) {
            return false
        }
        val invAmount = inventory.getInt(item)
        if (invAmount > amount) {
            inventory[item] = invAmount - amount
            return true
        }
        inventory.removeInt(item)
        return invAmount == amount
    }

    override fun consume(contextMap: ChoiceConsumerContextMap) {
        val context = contextMap[consumer]
        context.add(item, amount)
    }

    override fun displayItemStack(settings: BasicMenuSettings): ItemStack {
        // 生成原始的物品堆叠 (最终给予玩家的物品堆叠)
        val itemStack = item.createItemStack(amount)

        // 基于合成站渲染物品, 这将填充 name & lore
        itemStack.render()

        // 解析展示用的物品堆叠信息
        val slotDisplayResolved = settings.getSlotDisplay("choice").resolve {
            standard { component("item_name", itemStack.itemNameOrType) }
            folded("item_lore", itemStack.fastLoreOrEmpty)
        }

        // 应用解析结果
        return slotDisplayResolved.applyInPlace(itemStack)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 经验值类型的合成站输入.
 */
internal data class ExpChoice(
    val amount: Int,
) : RecipeChoice {
    companion object {
        const val TYPE: String = "exp"
    }

    override val checker: ExpChoiceChecker = ExpChoiceChecker
    override val consumer: ExpChoiceConsumer = ExpChoiceConsumer

    override fun check(contextMap: ChoiceCheckerContextMap): Boolean {
        val context = contextMap[checker]
        context.experienceSnapshot -= amount
        return context.experienceSnapshot >= 0
    }

    override fun consume(contextMap: ChoiceConsumerContextMap) {
        val context = contextMap[consumer]
        context.add(amount)
    }

    override fun displayItemStack(settings: BasicMenuSettings): ItemStack {
        // 经验瓶无需解析, 可以直接返回一个原始的物品堆叠
        return ItemStack(Material.EXPERIENCE_BOTTLE)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 方便函数.
 */
private fun ItemStack.render(): ItemStack {
    val context = CraftingStationContext(Pos.CHOICE)
    ItemRenderers.CRAFTING_STATION.render(this, context)
    return this
}