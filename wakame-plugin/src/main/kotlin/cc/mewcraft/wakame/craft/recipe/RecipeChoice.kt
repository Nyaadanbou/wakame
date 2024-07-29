package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成配方的输入.
 * 表现为合成输入gui中一格的物品.
 */
sealed interface RecipeChoice : Examinable {
    fun toBukkitItemStacks(): List<ItemStack>
}

/**
 * 单物品输入.
 */
data class SingleRecipeChoice(
    val choice: Key
) : RecipeChoice {
    override fun toBukkitItemStacks(): List<ItemStack> {
        TODO("Not yet implemented")
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
    val choices: List<Key>
) : RecipeChoice {
    override fun toBukkitItemStacks(): List<ItemStack> {
        TODO("Not yet implemented")
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
        val rawScalar = node.rawScalar()
        if (rawScalar != null) {
            val rawStr = rawScalar.toString()
            val choice = Key.key(rawStr)
            return SingleRecipeChoice(choice)
        }

        if (node.isList) {
            val choices = node.krequire<List<Key>>()
            return MultiRecipeChoice(choices)
        }

        throw SerializationException(node, type, "Unrecognized recipe choice")
    }
}
