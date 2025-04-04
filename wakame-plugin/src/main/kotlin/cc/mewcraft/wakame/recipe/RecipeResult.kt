package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.require
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成配方的输出.
 * 表现为合成输出gui中一格的物品.
 */
sealed interface RecipeResult : Examinable {
    fun toBukkitItemStack(): ItemStack
}

/**
 * 空输出.
 * 正常配方必然存在输出.
 * 此单例仅作特殊用途.
 */
data object EmptyRecipeResult : RecipeResult {
    override fun toBukkitItemStack(): ItemStack {
        return ItemStack(Material.AIR)
    }
}

/**
 * 单物品输出.
 */
data class SingleRecipeResult(
    val item: ItemX,
    val amount: Int,
) : RecipeResult {
    override fun toBukkitItemStack(): ItemStack {
        val itemstack = item.createItemStack() ?: throw IllegalArgumentException("Unknown item: '${item.key}'")
        itemstack.setData(DataComponentTypes.ITEM_MODEL, item.key)
        itemstack.amount = amount
        return itemstack
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * [RecipeResult] 的序列化器.
 */
internal object RecipeResultSerializer : TypeSerializer2<RecipeResult> {
    override fun deserialize(type: Type, node: ConfigurationNode): RecipeResult {
        val item = node.node("item").require<ItemX>()
        val amount = node.node("amount").getInt(1).apply {
            require(this >= 1) { "Item amount should not less than 1" }
        }
        return SingleRecipeResult(item, amount)
    }
}