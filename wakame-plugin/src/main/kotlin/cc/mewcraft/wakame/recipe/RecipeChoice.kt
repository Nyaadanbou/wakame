package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.mixin.support.recipe.KoishIngredient
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.MojangIngredient
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream
import org.bukkit.inventory.RecipeChoice as BukkitRecipeChoice

/**
 * 合成配方的输入.
 * 表现为合成输入 GUI 中一格的物品.
 */
sealed interface RecipeChoice : Examinable {
    fun toBukkitRecipeChoice(): BukkitRecipeChoice
    fun toMojangIngredient() : MojangIngredient
}

/**
 * 空输入.
 * 在某些配方中起占位作用, 如允许部分输入为空的锻造台转化配方.
 */
data object EmptyRecipeChoice : RecipeChoice {
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        return BukkitRecipeChoice.empty()
    }

    override fun toMojangIngredient(): MojangIngredient {
        throw UnsupportedOperationException("Unable to create empty ingredient")
    }
}

/**
 * 单物品输入.
 */
data class SingleRecipeChoice(
    val item: ItemRef,
) : RecipeChoice {
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        val itemstack = item.createItemStack()
        return BukkitRecipeChoice.ExactChoice(itemstack)
    }

    override fun toMojangIngredient(): MojangIngredient {
        return KoishIngredient.ofIdentifiers(setOf(item.id))
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 多物品输入.
 */
data class MultiRecipeChoice(
    val items: List<ItemRef>,
) : RecipeChoice {
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        val itemstacks = items.map { it.createItemStack() }
        return BukkitRecipeChoice.ExactChoice(itemstacks)
    }

    override fun toMojangIngredient(): MojangIngredient {
        return KoishIngredient.ofIdentifiers(items.map { it.id }.toSet())
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("items", items),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * [RecipeChoice] 的序列化器.
 */
internal object RecipeChoiceSerializer : TypeSerializer2<RecipeChoice> {
    override fun deserialize(type: Type, node: ConfigurationNode): RecipeChoice {
        // RecipeChoice 中的 ItemRef 必然是合法有效的, 因为无效的会在这里产生序列化异常
        val itemList = node.getList<ItemRef>(emptyList())
        return when (itemList.size) {
            0 -> throw SerializationException(node, type, "Recipe choice must have at least 1 element")
            1 -> SingleRecipeChoice(itemList[0])
            else -> MultiRecipeChoice(itemList)
        }
    }
}
