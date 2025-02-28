package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.adventure.toNamespacedKey
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Bukkit
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.Collections.emptyList
import java.util.stream.Stream
import org.bukkit.inventory.BlastingRecipe as BukkitBlastingRecipe
import org.bukkit.inventory.CampfireRecipe as BukkitCampfireRecipe
import org.bukkit.inventory.FurnaceRecipe as BukkitFurnaceRecipe
import org.bukkit.inventory.ShapedRecipe as BukkitShapredRecipe
import org.bukkit.inventory.ShapelessRecipe as BukkitShapelessRecipe
import org.bukkit.inventory.SmithingTransformRecipe as BukkitSmithingTransformRecipe
import org.bukkit.inventory.SmithingTrimRecipe as BukkitSmithingTrimRecipe
import org.bukkit.inventory.SmokingRecipe as BukkitSmokingRecipe
import org.bukkit.inventory.StonecuttingRecipe as BukkitStonecuttingRecipe

/**
 * 合成配方 (对 Bukkit 的合成配方的包装).
 */
// TODO 重命名为 MinecraftRecipe?
//  原因: Vanilla 一词的意思是未经修改的内容. 而 Minecraft 早在 1.16 开始就引入了数据包, 支持数据驱动添加新的游戏内容,
//  因此用 Vanilla 来描述这里所指的配方并不准确. 使用 Minecraft 来描述更加合适 - 强调其来自于这个游戏本身所创造的东西.
sealed interface VanillaRecipe : Keyed, Examinable {
    val result: RecipeResult

    fun registerBukkitRecipe(): Boolean

    fun unregisterBukkitRecipe() {
        Bukkit.removeRecipe(key.toNamespacedKey, false)
    }
}

/**
 * 高炉配方.
 */
class BlastingRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : VanillaRecipe {
    override fun registerBukkitRecipe(): Boolean {
        val blastingRecipe = BukkitBlastingRecipe(
            key.toNamespacedKey,
            result.toBukkitItemStack(),
            input.toBukkitRecipeChoice(),
            exp,
            cookingTime
        )
        return Bukkit.addRecipe(blastingRecipe)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("input", input),
        ExaminableProperty.of("cooking_time", cookingTime),
        ExaminableProperty.of("exp", exp)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 营火配方.
 */
class CampfireRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : VanillaRecipe {
    override fun registerBukkitRecipe(): Boolean {
        val campfireRecipe = BukkitCampfireRecipe(
            key.toNamespacedKey,
            result.toBukkitItemStack(),
            input.toBukkitRecipeChoice(),
            exp,
            cookingTime
        )
        return Bukkit.addRecipe(campfireRecipe)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("input", input),
        ExaminableProperty.of("cooking_time", cookingTime),
        ExaminableProperty.of("exp", exp)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 熔炉配方.
 */
class FurnaceRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : VanillaRecipe {
    override fun registerBukkitRecipe(): Boolean {
        val furnaceRecipe = BukkitFurnaceRecipe(
            key.toNamespacedKey,
            result.toBukkitItemStack(),
            input.toBukkitRecipeChoice(),
            exp,
            cookingTime
        )
        return Bukkit.addRecipe(furnaceRecipe)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("input", input),
        ExaminableProperty.of("cooking_time", cookingTime),
        ExaminableProperty.of("exp", exp)
    )


    override fun toString(): String = toSimpleString()
}

/**
 * 工作台有序合成配方.
 */
class ShapedRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val pattern: Array<String>,
    val ingredients: Map<Char, RecipeChoice>,
) : VanillaRecipe {
    companion object {
        const val EMPTY_INGREDIENT_CHAR = 'X'
    }

    override fun registerBukkitRecipe(): Boolean {
        val shapedRecipe = BukkitShapredRecipe(key.toNamespacedKey, result.toBukkitItemStack())
        pattern.forEachIndexed { i, s -> pattern[i] = s.replace(EMPTY_INGREDIENT_CHAR, ' ') }
        shapedRecipe.shape(*pattern)
        ingredients.forEach {
            shapedRecipe.setIngredient(it.key, it.value.toBukkitRecipeChoice())
        }
        return Bukkit.addRecipe(shapedRecipe, false)
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
    val ingredients: List<RecipeChoice>,
) : VanillaRecipe {
    override fun registerBukkitRecipe(): Boolean {
        val shapelessRecipe = BukkitShapelessRecipe(key.toNamespacedKey, result.toBukkitItemStack())
        ingredients.forEach {
            shapelessRecipe.addIngredient(it.toBukkitRecipeChoice())
        }
        return Bukkit.addRecipe(shapelessRecipe, false)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("ingredients", ingredients),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 锻造台转化配方.
 */
class SmithingTransformRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val base: RecipeChoice,
    val addition: RecipeChoice,
    val template: RecipeChoice,
) : VanillaRecipe {
    override fun registerBukkitRecipe(): Boolean {
        val smithingTransformRecipe = BukkitSmithingTransformRecipe(
            key.toNamespacedKey,
            result.toBukkitItemStack(),
            template.toBukkitRecipeChoice(),
            base.toBukkitRecipeChoice(),
            addition.toBukkitRecipeChoice(),
            false
        )
        return Bukkit.addRecipe(smithingTransformRecipe)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("base", base),
        ExaminableProperty.of("addition", addition),
        ExaminableProperty.of("template", template)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 锻造台纹饰配方.
 */
class SmithingTrimRecipe(
    override val key: Key,
    val base: RecipeChoice,
    val addition: RecipeChoice,
    val template: RecipeChoice,
) : VanillaRecipe {
    // 锻造台纹饰配方的结果是原版生成的
    override val result: RecipeResult = EmptyRecipeResult
    override fun registerBukkitRecipe(): Boolean {
        val smithingTrimRecipe = BukkitSmithingTrimRecipe(
            key.toNamespacedKey,
            template.toBukkitRecipeChoice(),
            base.toBukkitRecipeChoice(),
            addition.toBukkitRecipeChoice(),
            true
        )
        return Bukkit.addRecipe(smithingTrimRecipe)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("base", base),
        ExaminableProperty.of("addition", addition),
        ExaminableProperty.of("template", template)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 烟熏炉配方.
 */
class SmokingRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : VanillaRecipe {
    override fun registerBukkitRecipe(): Boolean {
        val smokingRecipe = BukkitSmokingRecipe(
            key.toNamespacedKey,
            result.toBukkitItemStack(),
            input.toBukkitRecipeChoice(),
            exp,
            cookingTime
        )
        return Bukkit.addRecipe(smokingRecipe)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("input", input),
        ExaminableProperty.of("cooking_time", cookingTime),
        ExaminableProperty.of("exp", exp)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 切石机配方.
 */
class StonecuttingRecipe(
    override val key: Key,
    override val result: RecipeResult,
    val input: RecipeChoice,
) : VanillaRecipe {
    override fun registerBukkitRecipe(): Boolean {
        val stonecuttingRecipe = BukkitStonecuttingRecipe(
            key.toNamespacedKey,
            result.toBukkitItemStack(),
            input.toBukkitRecipeChoice(),
        )
        return Bukkit.addRecipe(stonecuttingRecipe)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("input", input)
    )

    override fun toString(): String = toSimpleString()
}

enum class RecipeType(
    private val bridge: RecipeTypeBridge<*>,
) {
    BLASTING(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val input = node.node("input").require<RecipeChoice>()
        val cookingTime = node.node("cooking_time").getInt(100)
        val exp = node.node("exp").getFloat(0F)

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        BlastingRecipe(
            key = key,
            result = result,
            input = input,
            cookingTime = cookingTime,
            exp = exp
        )
    }),
    CAMPFIRE(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val input = node.node("input").require<RecipeChoice>()
        val cookingTime = node.node("cooking_time").getInt(100)
        val exp = node.node("exp").getFloat(0F)

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        CampfireRecipe(
            key = key,
            result = result,
            input = input,
            cookingTime = cookingTime,
            exp = exp
        )
    }),
    FURNACE(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val input = node.node("input").require<RecipeChoice>()
        val cookingTime = node.node("cooking_time").getInt(200)
        val exp = node.node("exp").getFloat(0F)

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        FurnaceRecipe(
            key = key,
            result = result,
            input = input,
            cookingTime = cookingTime,
            exp = exp
        )
    }),
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
                mapChild.require<RecipeChoice>()
            }

        //判断特殊字符是否被误用
        require(!ingredients.keys.contains(ShapedRecipe.EMPTY_INGREDIENT_CHAR)) { "${ShapedRecipe.EMPTY_INGREDIENT_CHAR} means an empty slot in pattern, should not be used as an ingredient char" }

        //判断 pattern 中的 key 是否都在 ingredients 中
        val missingIngredientKeys = pattern
            .map { it.toCharArray() }
            .reduce { acc, chars -> acc + chars }
            .distinct()
            .filter { it !in ingredients.keys && it != ShapedRecipe.EMPTY_INGREDIENT_CHAR }
        if (missingIngredientKeys.isNotEmpty()) {
            throw SerializationException("The keys [${missingIngredientKeys.joinToString(prefix = "'", postfix = "'")}] are specified in the pattern but not present in the ingredients")
        }

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        ShapedRecipe(
            key = key,
            result = result,
            pattern = pattern.toTypedArray(),
            ingredients = ingredients,
        )
    }),
    SHAPELESS(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val ingredients = node.node("ingredients")
            .childrenMap()
            .mapValues { (_, mapChild) ->
                mapChild.require<RecipeChoice>()
            }
            .values
            .toList()
            .apply {
                require(isNotEmpty()) { "Ingredients is not present" }
                require(size <= 9) { "Ingredients should not be more than 9" }
            }


        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        ShapelessRecipe(
            key = key,
            result = result,
            ingredients = ingredients
        )
    }),
    SMITHING_TRANSFORM(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val base = node.node("base").require<RecipeChoice>()
        val addition = node.node("addition").get<RecipeChoice>(EmptyRecipeChoice)
        val template = node.node("template").get<RecipeChoice>(EmptyRecipeChoice)

        // addition和template不能同时是EmptyRecipeChoice
        require(addition != EmptyRecipeChoice || template != EmptyRecipeChoice) {
            "Addition and template cannot be empty at the same time"
        }

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        SmithingTransformRecipe(
            key = key,
            result = result,
            base = base,
            addition = addition,
            template = template
        )
    }),
    SMITHING_TRIM(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val base = node.node("base").require<RecipeChoice>()
        val addition = node.node("addition").get<RecipeChoice>(EmptyRecipeChoice)
        val template = node.node("template").get<RecipeChoice>(EmptyRecipeChoice)

        // addition和template不能同时是EmptyRecipeChoice
        require(addition != EmptyRecipeChoice || template != EmptyRecipeChoice) {
            "`addition` and `template` cannot be empty at the same time"
        }

        val key = node.getRecipeKey()

        SmithingTrimRecipe(
            key = key,
            base = base,
            addition = addition,
            template = template
        )
    }),
    SMOKING(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val input = node.node("input").require<RecipeChoice>()
        val cookingTime = node.node("cooking_time").getInt(100)
        val exp = node.node("exp").getFloat(0F)

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        SmokingRecipe(
            key = key,
            result = result,
            input = input,
            cookingTime = cookingTime,
            exp = exp
        )
    }),
    STONECUTTING(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val input = node.node("input").require<RecipeChoice>()

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        StonecuttingRecipe(
            key = key,
            result = result,
            input = input,
        )
    });

    fun deserialize(node: ConfigurationNode): VanillaRecipe {
        val typeToken = bridge.typeToken
        val serializer = bridge.serializer
        return serializer.deserialize(typeToken.type, node)
    }
}

private fun ConfigurationNode.getRecipeKey(): Key {
    return this.hint(RepresentationHints.MINECRAFT_RECIPE_ID) ?: throw SerializationException(
        "The hint ${RepresentationHints.MINECRAFT_RECIPE_ID.identifier()} is not present"
    )
}

/**
 * 一个容器, 封装了 [typeToken] 和 [serializer].
 */
internal class RecipeTypeBridge<T : VanillaRecipe>(
    val typeToken: TypeToken<T>,
    val serializer: TypeSerializer<T>,
) {
    constructor(
        typeToken: TypeToken<T>,
        serializer: (Type, ConfigurationNode) -> T,
    ) : this(
        typeToken,
        TypeSerializer<T> { type, node -> serializer(type, node) }
    )
}

/**
 * [VanillaRecipe] 的序列化器.
 */
internal object VanillaRecipeSerializer : TypeSerializer<VanillaRecipe> {
    /**
     * ## Node structure
     * ```yaml
     * type: <recipe type>
     * (剩下的取决于 Recipe 具体实现)
     * ```
     */
    override fun deserialize(type: Type, node: ConfigurationNode): VanillaRecipe {
        val recipeType = node.node("type").require<RecipeType>()
        val recipe = recipeType.deserialize(node)
        return recipe
    }
}
