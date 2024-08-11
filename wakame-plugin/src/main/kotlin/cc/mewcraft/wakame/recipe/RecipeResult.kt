package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成配方的输出.
 * 表现为合成输出gui中一格的物品.
 */
interface RecipeResult : Examinable {
    fun toBukkitItemStack(): ItemStack
}

/**
 * 单物品输出.
 */
data class SingleRecipeResult(
    val result: ItemX,
    val amount: Int
) : RecipeResult {
    override fun toBukkitItemStack(): ItemStack {
        return result.createItemStack() ?: throw IllegalArgumentException("Unknown item: '${result.key}'")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("result", result),
        ExaminableProperty.of("amount", amount),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * [RecipeResult] 的序列化器.
 */
internal object RecipeResultSerializer : TypeSerializer<RecipeResult> {
    override fun deserialize(type: Type, node: ConfigurationNode): RecipeResult {
        val item = node.node("item").krequire<ItemX>()
        val amount = node.node("amount").getInt(1).apply {
            require(this >= 1) { "Item amount should not less than 1" }
        }
        return SingleRecipeResult(item, amount)
    }
}