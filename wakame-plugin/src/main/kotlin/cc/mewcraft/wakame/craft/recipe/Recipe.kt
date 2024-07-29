package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toNamespacedKey
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Bukkit
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream
import org.bukkit.inventory.ShapedRecipe as BukkitShapredRecipe
import org.bukkit.inventory.ShapelessRecipe as BukkitShapelessRecipe

/**
 * 合成配方.
 * 是对Bukkit的合成配方的包装.
 */
sealed interface Recipe : Keyed, Examinable {
    val result: RecipeResult

    fun addBukkitRecipe()

    fun removeBukkitRecipe() {
        Bukkit.removeRecipe(key.toNamespacedKey, false)
    }
}

//class BlastingRecipe : Recipe
//class CampfireRecipe : Recipe
//
///**
// * 熔炉配方.
// */
//class FurnaceRecipe(
//    override val key: Key,
//    override val result: RecipeResult
//    val
//
//) : Recipe

/**
 * 工作台有序合成配方.
 */
class ShapedRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val pattern: Array<String>,
    val ingredients: Map<Char, RecipeChoice>
) : Recipe {
    override fun addBukkitRecipe() {
        val shapedRecipe = BukkitShapredRecipe(key.toNamespacedKey, result.toBukkitItemStack())
        //TODO 'X'改为全局变量
        pattern.forEach { it.replace('X', ' ') }
        shapedRecipe.shape(*pattern)
        ingredients.forEach {
            shapedRecipe.setIngredient(it.key, org.bukkit.inventory.RecipeChoice.ExactChoice(it.value.toBukkitItemStacks()))
        }
        //在服务端添加配方，但不向玩家发送配方刷新包
        //所有配方均添加完毕后，统一刷新
        Bukkit.addRecipe(shapedRecipe, false)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("pattern", pattern),
        ExaminableProperty.of("ingredients", ingredients),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 工作台无序合成.
 */
class ShapelessRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val ingredients: List<RecipeChoice>
) : Recipe {
    override fun addBukkitRecipe() {
        val shapelessRecipe = BukkitShapelessRecipe(key.toNamespacedKey, result.toBukkitItemStack())
        ingredients.forEach {
            shapelessRecipe.addIngredient(org.bukkit.inventory.RecipeChoice.ExactChoice(it.toBukkitItemStacks()))
        }
        Bukkit.addRecipe(shapelessRecipe, false)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("ingredients", ingredients),
    )

    override fun toString(): String = toSimpleString()
}

//class SmithingTransformRecipe : Recipe
//
//class SmokingRecipe : Recipe
//class StonecuttingRecipe : Recipe

enum class RecipeType(
    private val bridge: RecipeTypeBridge<*>,
) {
    /*BLASTING(RecipeTypeBridge(typeTokenOf()) { type, node ->
        BlastingRecipe()
    }),
    CAMPFIRE(RecipeTypeBridge(typeTokenOf()) { type, node ->
        CampfireRecipe()
    }),
    FURNACE(RecipeTypeBridge(typeTokenOf()) { type, node ->
        FurnaceRecipe()
    }),*/
    SHAPED(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val pattern = node.node("pattern").getList<String>(emptyList()).apply {
            require(isNotEmpty()) { "The pattern is not present" }
            require(size <= 3) { "The pattern should be 1, 2 or 3 rows, not $size" }
            var lastLength = -1
            this.forEach {
                require(it.length in 1..3) { "The pattern rows should be 1, 2, or 3 characters, not ${it.length}" }
                require(lastLength == -1 || lastLength == it.length) { "The pattern must be rectangular" }
                lastLength = it.length
            }
        }
        val ingredients = node.node("ingredients")
            .childrenMap()
            .mapKeys { (nodeKey, _) ->
                nodeKey.toString().toCharArray().firstOrNull() ?: throw SerializationException("Can't read the ingredient key")
            }
            .mapValues { (_, mapChild) ->
                mapChild.krequire<RecipeChoice>()
            }

        //判断 X 特殊字符是否被误用
        require(!ingredients.keys.contains('X')) { "'X' means an empty slot in pattern, should not be used as an ingredient char" }
        //TODO 'X'改为全局变量

        //判断 pattern 中的 key 是否都在 ingredients 中
        val missingIngredientKeys = pattern
            .map { it.toCharArray() }
            .reduce { acc, chars -> acc + chars }
            .filter { it !in ingredients.keys && it != 'X' }
        if (missingIngredientKeys.isNotEmpty()) {
            throw SerializationException("The keys [${missingIngredientKeys.joinToString()}] are specified in the pattern but not present in the ingredients")
        }

        val result = node.node("result").krequire<RecipeResult>()
        val key = node.getRecipeKey()

        ShapedRecipe(
            key = key,
            result = result,
            pattern = pattern.toTypedArray(),
            ingredients = ingredients,
        )
    }),
    SHAPELESS(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val ingredients = node.node("ingredients").getList<RecipeChoice>(emptyList()).apply {
            require(isNotEmpty()) { "Ingredients is not present" }
            require(size <= 9) { "Ingredients should not be more than 9" }
        }

        val result = node.node("result").krequire<RecipeResult>()
        val key = node.getRecipeKey()

        ShapelessRecipe(
            key = key,
            result = result,
            ingredients = ingredients
        )
    }),
    /*SMITHING_TRANSFORM(RecipeTypeBridge(typeTokenOf()) { type, node ->
        SmithingTrimRecipe()
    })
    SMOKING(RecipeTypeBridge(typeTokenOf()) { type, node ->
        SmokingRecipe()
    }),
    STONECUTTING(RecipeTypeBridge(typeTokenOf()) { type, node ->
        StonecuttingRecipe()
    })*/;

    fun deserialize(node: ConfigurationNode): Recipe {
        val typeToken = bridge.typeToken
        val serializer = bridge.serializer
        return serializer.deserialize(typeToken.type, node)
    }
}

private fun ConfigurationNode.getRecipeKey(): Key {
    return this.hint(RecipeSerializer.HINT_NODE) ?: throw SerializationException(
        "The hint node for recipe key is not present"
    )
}

/**
 * 一个容器, 封装了 [typeToken] 和 [serializer].
 */
internal class RecipeTypeBridge<T : Recipe>(
    val typeToken: TypeToken<T>,
    val serializer: TypeSerializer<T>,
) {
    constructor(typeToken: TypeToken<T>, serializer: (Type, ConfigurationNode) -> T) : this(typeToken, object : TypeSerializer<T> {
        override fun deserialize(type: Type, node: ConfigurationNode): T {
            return serializer(type, node)
        }
    })
}

/**
 * [Recipe] 的序列化器.
 */
internal object RecipeSerializer : TypeSerializer<Recipe> {
    val HINT_NODE: RepresentationHint<Key> = RepresentationHint.of("key", typeTokenOf<Key>())

    /**
     * ## Node structure
     * ```yaml
     * type: <recipe type>
     * (剩下的取决于 Recipe 具体实现)
     * ```
     */
    override fun deserialize(type: Type, node: ConfigurationNode): Recipe {
        val recipeType = node.node("type").krequire<RecipeType>()
        val recipe = recipeType.deserialize(node)
        return recipe
    }
}
