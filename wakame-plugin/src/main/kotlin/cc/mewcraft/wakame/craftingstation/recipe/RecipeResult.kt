package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext.Pos
import cc.mewcraft.wakame.gui.BasicMenuSettings
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
     * 将此 [RecipeResult] 的效果应用到玩家身上.
     * 所产生的效果包括但不仅限于: 扣除材料, 给予物品, 给予经验.
     */
    fun apply(player: Player)

    /**
     * 检查此 [RecipeResult] 是否有效.
     * 用于延迟验证配方是否能够注册.
     */
    fun valid(): Boolean

    /**
     * 获取此 [RecipeResult] 展示用的物品堆叠.
     *
     * 由于合成结果不一定是固定的, 其展示用的物品堆叠与最终给予玩家的物品堆叠必须要有区别.
     * 原因是当一个物品基于某种规则随机生成时, 其展示用的物品堆叠应该向玩家表达清楚随机的规则,
     * 而不是直接展示众多随机结果中的一种. 这样可以使玩家对合成的随机结果有更清晰的认识.
     */
    fun displayItemStack(settings: BasicMenuSettings): ItemStack
}

/**
 * 物品类型的合成站输出.
 */
internal data class ItemResult(
    val item: ItemX,
    val amount: Int,
) : RecipeResult {
    override fun apply(player: Player) {
        player.giveItemStack(item.createItemStack(amount, player))
    }

    override fun valid(): Boolean {
        return item.valid()
    }

    override fun displayItemStack(settings: BasicMenuSettings): ItemStack {
        // 开发日记 2024/12/27 小米:
        // 合成配方的[结果]不需要再套一层 SlotDisplay 的逻辑,
        // 只需要用合成站的 ItemRenderer 把物品渲染一下就行.
        // 但如果实在有这个需求的话, 也可以写.

        // 生成原始的物品堆叠
        val itemStack = item.createItemStack(amount) ?: ItemRegistry.ERROR_ITEM_STACK

        // 然后基于合成站来渲染物品, 主要填充 name & lore
        itemStack.render()

        // 然后基于合成站渲染并返回
        return itemStack
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