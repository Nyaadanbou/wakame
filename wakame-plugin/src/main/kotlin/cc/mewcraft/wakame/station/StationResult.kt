package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.util.giveItemStack
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成站的输出.
 */
sealed interface StationResult : Examinable {
    /**
     * 执行此 [StationResult] 的效果
     */
    fun apply(player: Player)

    /**
     * 该 [StationResult] 是否有效
     * 用于延迟验证配方是否能够注册
     */
    fun isValid(): Boolean

    /**
     * 获取此 [StationResult] 的描述
     * 使用MiniMessage格式
     */
    fun description(stationLayout: StationLayout): String

    /**
     * 获取此 [StationResult] 的Gui物品
     */
    fun guiItemStack(): ItemStack

}

/**
 * 物品类型的合成站输出.
 */
data class ItemResult(
    val item: ItemX,
    val amount: Int
) : StationResult {
    override fun apply(player: Player) {
        val itemStack = item.createItemStack(player)
        itemStack?.amount = amount
        player.giveItemStack(itemStack)
    }

    override fun isValid(): Boolean {
        return item.createItemStack() != null
    }

    override fun description(stationLayout: StationLayout): String {
        // 缺省构建格式: "材料 *1"
        return stationLayout.resultsLang["item"]
            ?.replace("<render_name>", item.renderName())
            ?.replace("<amount>", amount.toString())
            ?: "${item.renderName()} <white>×$amount</white>"
    }

    override fun guiItemStack(): ItemStack {
        // TODO 使用gui物品
        val itemStack = item.createItemStack() ?: ItemStack(Material.BARRIER)
        itemStack.amount = amount
        return itemStack
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