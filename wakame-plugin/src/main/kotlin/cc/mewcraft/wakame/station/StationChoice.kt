package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 工作站的一项输入.
 */
sealed interface StationChoice : Examinable {
    val description: Component
    val matcher: StationChoiceMatcher<*>
    fun checkState(contextMap: StationChoiceMatcherContextMap): StationChoiceCheckState
}


/* Internals */


/**
 * 物品类型的工作站输入.
 */
internal data class ItemChoice(
    val item: ItemX,
    val amount: Int,
) : StationChoice {
    companion object {
        const val TYPE: String = "item"
    }

    override val description: Component = TODO()
    override val matcher: ItemChoiceMatcher = ItemChoiceMatcher

    override fun checkState(contextMap: StationChoiceMatcherContextMap): StationChoiceCheckState {
        val context = contextMap[matcher]
        val inventory = context.inventorySnapshot

        if (!inventory.containsKey(item)) {
            return StationChoiceCheckState(this, false)
        }
        val invAmount = inventory.getInt(item)
        if (invAmount > amount) {
            inventory[item] = invAmount - amount
            return StationChoiceCheckState(this, true)
        }
        inventory.removeInt(item)
        return StationChoiceCheckState(this, invAmount == amount)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String =
        toSimpleString()
}

/**
 * 经验值类型的工作站输入.
 */
internal data class ExpChoice(
    val amount: Int
) : StationChoice {
    companion object {
        const val TYPE: String = "exp"
    }

    override val description: Component = TODO()
    override val matcher: ExpChoiceMatcher = ExpChoiceMatcher

    override fun checkState(contextMap: StationChoiceMatcherContextMap): StationChoiceCheckState {
        val context = contextMap[matcher]
        context.experienceSnapshot -= amount
        val sufficient = context.experienceSnapshot >= 0
        return StationChoiceCheckState(this, sufficient)
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