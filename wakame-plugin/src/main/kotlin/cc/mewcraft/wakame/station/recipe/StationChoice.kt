package cc.mewcraft.wakame.station.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXNeko
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.item.setSystemUse
import cc.mewcraft.wakame.item.tryNekoStack
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
sealed interface StationChoice : Examinable {
    val checker: ChoiceChecker<*>
    val consumer: ChoiceConsumer<*>

    /**
     * 通过上下文检查此 [StationChoice] 的满足与否
     * 上下文依靠 [ChoiceCheckerContextMap] 获取
     */
    fun check(contextMap: ChoiceCheckerContextMap): Boolean

    /**
     * 将此 [StationChoice] 的消耗添加到上下文中
     * 上下文依靠 [ChoiceConsumerContextMap] 获取
     */
    fun consume(contextMap: ChoiceConsumerContextMap)

    /**
     * 该 [StationChoice] 是否有效
     * 用于延迟验证配方是否能够注册
     */
    fun isValid(): Boolean

    /**
     * 获取此 [StationChoice] 的描述
     * 使用MiniMessage格式的字符串
     */
    fun description(layout: MenuLayout): String

    /**
     * 获取此 [StationChoice] 的展示物品
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

    override fun isValid(): Boolean {
        return item.isValid()
    }

    override fun description(layout: MenuLayout): String {
        // 缺省构建格式: "<prefix> 材料 ×1"
        return layout.getLang("choices.item")
            ?.replace("<render_name>", item.renderName())
            ?.replace("<amount>", amount.toString())
            ?: "<prefix> ${item.renderName()} ×$amount"
    }

    override fun displayItemStack(): ItemStack {
        // TODO gui物品
        val displayItemStack = item.createItemStack() ?: ItemStack(Material.BARRIER)
        if (item is ItemXNeko) {
            displayItemStack.tryNekoStack?.setSystemUse()
        }
        displayItemStack.amount = amount
        return displayItemStack
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String =
        toSimpleString()
}

/**
 * 经验值类型的合成站输入.
 */
internal data class ExpChoice(
    val amount: Int
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

    override fun isValid(): Boolean {
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
                val amount = node.node("amount").getInt(1).apply {
                    require(this > 0) { "Item amount must more than 0" }
                }
                return ItemChoice(item, amount)
            }

            ExpChoice.TYPE -> {
                val amount = node.node("amount").krequire<Int>().apply {
                    require(this > 0) { "Exp amount must more than 0" }
                }
                return ExpChoice(amount)
            }

            else -> {
                throw SerializationException("Unknown station choice type")
            }
        }
    }
}