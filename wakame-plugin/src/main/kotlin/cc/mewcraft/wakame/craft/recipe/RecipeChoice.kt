package cc.mewcraft.wakame.craft.recipe

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
import org.bukkit.inventory.RecipeChoice as BukkitRecipeChoice
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.*
import java.util.stream.Stream

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
    val choice: Key
) : RecipeChoice {
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        //TODO 临时实现
        when (choice.namespace()) {
            "minecraft" -> {
                val material = Material.getMaterial(choice.value().uppercase(Locale.getDefault()))
                material ?: throw IllegalArgumentException("Unknown vanilla item: '$choice'")
                return BukkitRecipeChoice.ExactChoice(ItemStack(material))
            }

            "wakame" -> {
                val nekoItem = ItemRegistry.CUSTOM.find(Key.key(choice.value().replaceFirst('/', ':')))
                val nekoStack = nekoItem?.realize()
                val itemStack = nekoStack?.itemStack
                itemStack ?: throw IllegalArgumentException("Unknown wakame item: '$choice'")
                return BukkitRecipeChoice.ExactChoice(itemStack)
            }

            else -> {
                throw IllegalArgumentException("Unknown namespace")
            }
        }
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
    override fun toBukkitRecipeChoice(): BukkitRecipeChoice {
        //TODO 临时实现
        val itemStacks: MutableList<ItemStack> = mutableListOf()
        choices.forEach {
            when (it.namespace()) {
                "minecraft" -> {
                    val material = Material.getMaterial(it.value().uppercase(Locale.getDefault()))
                    material ?: throw IllegalArgumentException("Unknown vanilla item: '$it'")
                    itemStacks.add(ItemStack(material))
                }

                "wakame" -> {
                    val nekoItem = ItemRegistry.CUSTOM.find(Key.key(it.value().replaceFirst('/', ':')))
                    val nekoStack = nekoItem?.realize()
                    val itemStack = nekoStack?.itemStack
                    itemStack ?: throw IllegalArgumentException("Unknown wakame item: '$it'")
                    itemStacks.add(itemStack)
                }

                else -> {
                    throw IllegalArgumentException("Unknown namespace")
                }
            }
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
