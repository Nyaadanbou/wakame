package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.item.toNMS
import cc.mewcraft.wakame.util.require
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
    fun toMojangStack(): MojangStack
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

    override fun toMojangStack(): MojangStack {
        return MojangStack.EMPTY
    }
}

/**
 * 单物品输出.
 */
data class SingleRecipeResult(
    val item: ItemRef,
    val amount: Int,
) : RecipeResult {
    override fun toBukkitItemStack(): ItemStack {
        val itemStack = item.createItemStack()
        itemStack.amount = amount
        return itemStack
    }

    override fun toMojangStack(): MojangStack {
        val itemStack = item.createItemStack()
        itemStack.amount = amount
        return itemStack.toNMS()
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
        val item = node.node("item").require<ItemRef>()
        val amount = node.node("amount").getInt(1).apply {
            require(this >= 1) { "Item amount should not less than 1" }
        }
        return SingleRecipeResult(item, amount)
    }
}