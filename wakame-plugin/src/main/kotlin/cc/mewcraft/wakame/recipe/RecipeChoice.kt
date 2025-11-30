package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.ItemTagManager
import cc.mewcraft.wakame.mixin.support.KoishIngredient
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangIngredient
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成配方的输入.
 * 表现为合成输入 GUI 中一格的物品.
 */
sealed interface RecipeChoice : Examinable {
    fun toMojangIngredient(): MojangIngredient
}

/**
 * 空配方输入.
 * 在某些配方中起占位作用, 如允许部分输入为空的锻造台转化配方.
 */
data object EmptyRecipeChoice : RecipeChoice {
    override fun toMojangIngredient(): MojangIngredient {
        throw UnsupportedOperationException("Unable to create empty ingredient")
    }
}

/**
 * 单物品配方输入.
 * 支持原版物品或 Koish 物品.
 */
data class SingleItemRecipeChoice(
    val item: ItemRef,
) : RecipeChoice {
    override fun toMojangIngredient(): MojangIngredient {
        return KoishIngredient.ofIdentifiers(setOf(item.id))
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("item", item),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 多物品配方输入.
 * 支持原版物品或 Koish 物品, 可以混合.
 */
data class MultiItemRecipeChoice(
    val items: List<ItemRef>,
) : RecipeChoice {
    override fun toMojangIngredient(): MojangIngredient {
        return KoishIngredient.ofIdentifiers(items.map { it.id }.toSet())
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("items", items),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 包含特定标签中所有物品的配方输入.
 * 此标签非原版标签, 而是 Koish 系统维护的一套标签系统.
 * @see cc.mewcraft.wakame.item.ItemTagManager
 */
data class TagRecipeChoice(
    val tagId: Identifier
) : RecipeChoice {
    override fun toMojangIngredient(): MojangIngredient {
        val items = ItemTagManager.getValues(tagId)
        // 找不到标签或标签为空, 则抛异常使相关配方注册失败
        // 一方面方便定位问题, 另一方面服务端本身也不允许创建空原料, 也会抛异常
        if (items.isEmpty()) {
            throw IllegalStateException("Tag '$tagId' does not exist or is empty.")
        }
        return KoishIngredient.ofIdentifiers(items.map { it.id }.toSet())
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("tag_id", tagId),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * [RecipeChoice] 的序列化器.
 */
internal object RecipeChoiceSerializer : TypeSerializer2<RecipeChoice> {
    override fun deserialize(type: Type, node: ConfigurationNode): RecipeChoice {
        val str = node.rawScalar().toString()
        // 以 `#` 开头的认为是一个标签
        if (str.startsWith("#")) {
            return TagRecipeChoice(Identifier.key(str.drop(1)))
        }
        // 注册成功的配方中的 ItemRef 必然是合法有效的, 因为无效的会在这里产生序列化异常
        val itemList = node.getList<ItemRef>(emptyList())
        return when (itemList.size) {
            0 -> throw SerializationException(node, type, "Recipe choice must have at least 1 element")
            1 -> SingleItemRecipeChoice(itemList[0])
            else -> MultiItemRecipeChoice(itemList)
        }
    }
}
