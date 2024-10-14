package cc.mewcraft.wakame.station.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXNeko
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.CraftingStationContext
import cc.mewcraft.wakame.display2.implementation.CraftingStationContext.*
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.util.*
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
     * 使用MiniMessage格式的字符串
     */
    fun description(layout: MenuLayout): String

    /**
     * 获取此 [StationResult] 的展示物品
     */
    fun displayItemStack(): ItemStack

}

/**
 * 物品类型的合成站输出.
 */
data class ItemResult(
    val item: ItemX,
    val amount: Int,
) : StationResult {
    override fun apply(player: Player) {
        val itemStack = item.createItemStack(player)
        itemStack?.amount = amount
        player.giveItemStack(itemStack)
    }

    override fun isValid(): Boolean {
        return item.isValid()
    }

    override fun description(layout: MenuLayout): String {
        // 缺省构建格式: "材料 ×1"
        return layout.getLang("results.item")
            ?.replace("<render_name>", item.renderName())
            ?.replace("<amount>", amount.toString())
            ?: "${item.renderName()} ×$amount"
    }

    override fun displayItemStack(): ItemStack {
        // TODO gui物品
        val displayItemStack = item.createItemStack() ?: ItemStack(Material.BARRIER)
        if (item is ItemXNeko) displayItemStack.render()
        displayItemStack.amount = amount
        return displayItemStack
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

/**
 * 方便函数.
 */
private fun ItemStack.render() {
    val nekoStack = tryNekoStack ?: return
    val context = CraftingStationContext(Pos.RESULT)
    ItemRenderers.CRAFTING_STATION.render(nekoStack, context)
}