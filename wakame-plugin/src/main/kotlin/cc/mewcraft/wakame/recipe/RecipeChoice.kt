package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.util.itemModel
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream
import org.bukkit.inventory.RecipeChoice as BukkitRecipeChoice

/**
 * 合成配方的输入.
 * 表现为合成输入gui中一格的物品.
 */
sealed interface RecipeChoice : Examinable {
    fun toBukkitRecipeChoice(): BukkitRecipeChoice
}

/**
 * 空输入.
 */
data object EmptyRecipeChoice : RecipeChoice {
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        return BukkitRecipeChoice.empty()
    }
}

/**
 * 单物品输入.
 */
data class SingleRecipeChoice(
    val choice: ItemX
) : RecipeChoice {
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        val itemStack = choice.createItemStack() ?: throw IllegalArgumentException("Unknown item: '${choice.key}'")
        itemStack.itemModel = choice.key
        return BukkitRecipeChoice.ExactChoice(itemStack)
    }


    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("choice", choice),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 多物品输入.
 */
data class MultiRecipeChoice(
    val choices: List<ItemX>
) : RecipeChoice {
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        val itemStacks: MutableList<ItemStack> = mutableListOf()
        choices.forEach {
            val itemStack = it.createItemStack() ?: throw IllegalArgumentException("Unknown item: '${it.key}'")
            itemStack.itemModel = it.key
            itemStacks.add(itemStack)
        }
        return BukkitRecipeChoice.ExactChoice(itemStacks)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("choices", choices),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * [RecipeChoice] 的序列化器.
 */
internal object RecipeChoiceSerializer : TypeSerializer<RecipeChoice> {
    override fun deserialize(type: Type, node: ConfigurationNode): RecipeChoice {
        val itemXList = node.getList<ItemX>(emptyList())
        return when (itemXList.size) {
            0 -> throw SerializationException(node, type, "Recipe choice must have at least 1 element")
            1 -> SingleRecipeChoice(itemXList[0])
            else -> MultiRecipeChoice(itemXList)
        }
    }
}
