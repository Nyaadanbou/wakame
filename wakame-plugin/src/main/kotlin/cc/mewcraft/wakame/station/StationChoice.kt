package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 工作站的一项输入.
 */
sealed interface StationChoice : Examinable {
    fun test(user: User<Player>): Boolean
    fun take(user: User<Player>)
}

/**
 * 物品类型的工作站输入.
 */
data class ItemChoice(
    val item: Key,
    val amount: Int
) : StationChoice {
    override fun test(user: User<Player>): Boolean {
        TODO("Not yet implemented")
    }

    override fun take(user: User<Player>) {
        TODO("Not yet implemented")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String = toSimpleString()
}


/**
 * [StationChoice] 的序列化器.
 */
internal object StationChoiceSerializer : TypeSerializer<StationChoice> {
    override fun deserialize(type: Type, node: ConfigurationNode): StationChoice {
        val item = node.node("item").krequire<Key>()
        val amount = node.node("amount").getInt(1).apply {
            require(this >= 1) { "Item amount should not less than 1" }
        }
        return ItemChoice(item, amount)
    }
}