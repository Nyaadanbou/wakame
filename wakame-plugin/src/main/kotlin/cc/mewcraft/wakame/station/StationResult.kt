package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 工作站的一项输出.
 */
sealed interface StationResult : Examinable {
    fun apply(user: User<Player>)

}

/**
 * 物品类型的工作站输出.
 */
data class ItemResult(
    val item: ItemX,
    val amount: Int
) : StationResult {
    override fun apply(user: User<Player>) {
        TODO("Not yet implemented")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String = toSimpleString()

}


/**
 * [StationResult] 的序列化器.
 */
internal object StationResultSerializer : TypeSerializer<StationResult> {
    override fun deserialize(type: Type, node: ConfigurationNode): StationResult {
        val item = node.node("item").krequire<ItemX>()
        val amount = node.node("amount").getInt(1).apply {
            require(this >= 1) { "Item amount should not less than 1" }
        }
        return ItemResult(item, amount)
    }
}