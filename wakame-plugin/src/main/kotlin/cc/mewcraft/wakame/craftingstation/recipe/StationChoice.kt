package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext.Pos
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
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
internal sealed interface StationChoice : Examinable {
    /**
     * 用于检查该 [StationChoice] 所要求的输入是否被满足.
     */
    val checker: ChoiceChecker<*>

    /**
     * 用于消耗该 [StationChoice] 所要求的输入 (从玩家身上).
     */
    val consumer: ChoiceConsumer<*>

    /**
     * 通过上下文检查此 [StationChoice] 的满足与否.
     * 上下文依靠 [ChoiceCheckerContextMap] 获取.
     */
    fun check(contextMap: ChoiceCheckerContextMap): Boolean

    /**
     * 将此 [StationChoice] 的消耗添加到上下文中.
     * 上下文依靠 [ChoiceConsumerContextMap] 获取.
     */
    fun consume(contextMap: ChoiceConsumerContextMap)

    /**
     * 该 [StationChoice] 是否有效.
     * 用于延迟验证配方是否能够注册.
     */
    fun valid(): Boolean

    /**
     * 获取此 [StationChoice] 的描述.
     * 使用 MiniMessage 格式的字符串.
     */
    fun description(layout: MenuLayout): String

    /**
     * 获取此 [StationChoice] 的展示物品.
     */
    fun displayItemStack(): ItemStack
}


/* Internals */


/**
 * 物品类型的合成站输入.
 */
internal data class ItemChoice(
    val item: ItemX,
    val amount: Int,
) : StationChoice {
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

    override fun valid(): Boolean {
        return item.valid()
    }

    override fun description(layout: MenuLayout): String {
        // 缺省构建格式: "<prefix> 材料 ×1"
        return layout.getLang("choices.item")
            ?.replace("<name>", item.displayName())
            ?.replace("<amount>", amount.toString())
            ?: "<prefix> ${item.displayName()} ×$amount"
    }

    override fun displayItemStack(): ItemStack {
        val displayItemStack = item.createItemStack() ?: ItemStack(Material.BARRIER)
        displayItemStack.render()
        displayItemStack.amount = amount
        return displayItemStack
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
) : StationChoice {
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

    override fun valid(): Boolean {
        return true
    }

    override fun description(layout: MenuLayout): String {
        // 缺省构建格式: "<prefix> EXP ×1"
        return layout.getLang("choices.exp")
            ?.replace("<amount>", amount.toString())
            ?: "<prefix> EXP ×$amount"
    }

    override fun displayItemStack(): ItemStack {
        return ItemStack(Material.EXPERIENCE_BOTTLE)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String =
        toSimpleString()
}

/**
 * [StationChoice] 的序列化器.
 */
internal object StationChoiceSerializer : TypeSerializer<StationChoice> {
    override fun deserialize(type: Type, node: ConfigurationNode): StationChoice {
        val choiceType = node.node("type").krequire<String>()
        when (choiceType) {
            ItemChoice.TYPE -> {
                val item = node.node("id").krequire<ItemX>()
                val amount = node.node("amount").getInt(1)
                require(amount > 0) { "Item amount must more than 0" }
                return ItemChoice(item, amount)
            }

            ExpChoice.TYPE -> {
                val amount = node.node("amount").krequire<Int>()
                require(amount > 0) { "Exp amount must more than 0" }
                return ExpChoice(amount)
            }

            else -> {
                throw SerializationException("Unknown station choice type")
            }
        }
    }
}

/**
 * 方便函数.
 */
private fun ItemStack.render(): ItemStack {
    val nekoStack = shadowNeko() ?: return this
    val context = CraftingStationContext(Pos.CHOICE, erase = true)
    ItemRenderers.CRAFTING_STATION.render(nekoStack, context)
    return this
}