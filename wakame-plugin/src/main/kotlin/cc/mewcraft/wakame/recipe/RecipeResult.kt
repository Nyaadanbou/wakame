package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.*
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
    val result: Key,
    val amount: Int
) : RecipeResult {
    override fun toBukkitItemStack(): ItemStack {
        //TODO 临时实现
        when (result.namespace()) {
            "minecraft" -> {
                val material = Material.getMaterial(result.value().uppercase(Locale.getDefault()))
                material ?: throw IllegalArgumentException("Unknown vanilla item: '$result'")
                return ItemStack(material, amount)
            }

            "wakame" -> {
                val nekoItem = ItemRegistry.CUSTOM.find(Key.key(result.value().replaceFirst('/', ':')))
                val nekoStack = nekoItem?.realize()
                val itemStack = nekoStack?.itemStack
                itemStack ?: throw IllegalArgumentException("Unknown wakame item: '$result'")
                itemStack.amount = amount
                return itemStack
            }

            else -> {
                throw IllegalArgumentException("Unknown namespace")
            }
        }
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
        val item = node.node("item").krequire<Key>()
        val amount = node.node("amount").getInt(1).apply {
            require(this >= 1)
        }
        return SingleRecipeResult(item, amount)
    }
}