package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext.Pos
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.giveItemStack
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成站的输出.
 */
internal sealed interface RecipeResult : Examinable {
    /**
     * 执行此 [RecipeResult] 的效果
     */
    fun apply(player: Player)

    /**
     * 该 [RecipeResult] 是否有效
     * 用于延迟验证配方是否能够注册
     */
    fun valid(): Boolean

    /**
     * 获取此 [RecipeResult] 的描述
     * 使用MiniMessage格式的字符串
     */
    fun description(layout: MenuLayout): String

    /**
     * 获取此 [RecipeResult] 的展示物品
     */
    fun displayItemStack(): ItemStack

}

/**
 * 物品类型的合成站输出.
 */
internal data class ItemResult(
    val item: ItemX,
    val amount: Int,
) : RecipeResult {
    override fun apply(player: Player) {
        val itemStack = item.createItemStack(player)
        itemStack?.amount = amount
        player.giveItemStack(itemStack)
    }

    override fun valid(): Boolean {
        return item.valid()
    }

    override fun description(layout: MenuLayout): String {
        // 缺省构建格式: "材料 ×1"
        return layout.getLang("results.item")
            ?.replace("<name>", item.displayName())
            ?.replace("<amount>", amount.toString())
            ?: "${item.displayName()} ×$amount"
    }

    override fun displayItemStack(): ItemStack {
        val displayItemStack = item.createItemStack() ?: ItemRegistry.ERROR_ITEM_STACK
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
 * [RecipeResult] 的序列化器.
 */
internal object StationResultSerializer : TypeSerializer<RecipeResult> {
    override fun deserialize(type: Type, node: ConfigurationNode): RecipeResult {
        val item = node.node("item").krequire<ItemX>()
        val amount = node.node("amount").getInt(1)
        require(amount >= 1) { "item amount should not less than 1" }
        return ItemResult(item, amount)
    }
}

/**
 * 方便函数.
 */
private fun ItemStack.render(): ItemStack {
    val nekoStack = shadowNeko() ?: return this
    val context = CraftingStationContext(Pos.RESULT, erase = true)
    ItemRenderers.CRAFTING_STATION.render(nekoStack, context)
    return this
}