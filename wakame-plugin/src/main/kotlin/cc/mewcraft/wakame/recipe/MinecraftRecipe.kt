package cc.mewcraft.wakame.recipe

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.adventure.key.Identified
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.adventure.toSimpleString
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.ShapedRecipePattern
import net.minecraft.world.item.crafting.TransmuteResult
import org.bukkit.craftbukkit.inventory.CraftRecipe
import org.bukkit.craftbukkit.inventory.trim.CraftTrimPattern
import org.bukkit.inventory.meta.trim.TrimPattern
import org.bukkit.inventory.recipe.CookingBookCategory
import org.bukkit.inventory.recipe.CraftingBookCategory
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.*
import java.util.Collections.emptyList
import java.util.stream.Stream
import net.minecraft.world.item.crafting.BlastingRecipe as MojangBlastingRecipe
import net.minecraft.world.item.crafting.CampfireCookingRecipe as MojangCampfireRecipe
import net.minecraft.world.item.crafting.CookingBookCategory as MojangCookingBookCategory
import net.minecraft.world.item.crafting.CraftingBookCategory as MojangCraftingBookCategory
import net.minecraft.world.item.crafting.ShapedRecipe as MojangShapedRecipe
import net.minecraft.world.item.crafting.ShapelessRecipe as MojangShapelessRecipe
import net.minecraft.world.item.crafting.SmeltingRecipe as MojangFurnaceRecipe
import net.minecraft.world.item.crafting.SmithingTransformRecipe as MojangSmithingTransformRecipe
import net.minecraft.world.item.crafting.SmithingTrimRecipe as MojangSmithingTrimRecipe
import net.minecraft.world.item.crafting.SmokingRecipe as MojangSmokingRecipe
import net.minecraft.world.item.crafting.StonecutterRecipe as MojangStonecuttingRecipe
import net.minecraft.world.item.equipment.trim.TrimPattern as MojangTrimPattern

/**
 * 配方 (对 nms 配方的包装).
 */
sealed interface MinecraftRecipe : Identified, Examinable {
    val result: RecipeResult

    /**
     * 将该配方添加到服务端的 RecipeManager 中.
     * 注意: 仅调用该方法并不会向玩家客户端重新发包.
     */
    fun addToManager()

    /**
     * 将该配方从服务端的 RecipeManager 中移除.
     * 注意: 仅调用该方法并不会向玩家客户端重新发包.
     */
    fun removeFromManager(): Boolean {
        return RECIPE_MANAGER.removeRecipe(identifier.createResourceKey())
    }

    /**
     * [MinecraftRecipe] 的序列化器.
     */
    object Serializer : SimpleSerializer<MinecraftRecipe> {
        /**
         * ## Node structure
         * ```yaml
         * type: <recipe type>
         * (剩下的取决于 Recipe 具体实现)
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): MinecraftRecipe? {
            val recipeType = node.node("type").require<RecipeType>()
            val recipe = recipeType.deserialize(node)
            return recipe
        }
    }
}

/**
 * 高炉配方.
 */
class BlastingRecipe(
    override val identifier: Identifier,
    override val result: RecipeResult,
    val group: String,
    val category: CookingBookCategory,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : MinecraftRecipe {
    override fun addToManager() {
        val mojangRecipe = MojangBlastingRecipe(
            group,
            category.toMojang(),
            input.toMojangIngredient(),
            result.toMojangStack(),
            exp,
            cookingTime
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
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
    override val identifier: Identifier,
    override val result: RecipeResult,
    val group: String,
    val category: CookingBookCategory,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : MinecraftRecipe {
    override fun addToManager() {
        val mojangRecipe = MojangCampfireRecipe(
            group,
            category.toMojang(),
            input.toMojangIngredient(),
            result.toMojangStack(),
            exp,
            cookingTime
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
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
    override val identifier: Identifier,
    override val result: RecipeResult,
    val group: String,
    val category: CookingBookCategory,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : MinecraftRecipe {
    override fun addToManager() {
        val mojangRecipe = MojangFurnaceRecipe(
            group,
            category.toMojang(),
            input.toMojangIngredient(),
            result.toMojangStack(),
            exp,
            cookingTime
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
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
    override val identifier: Identifier,
    override val result: RecipeResult,
    val group: String,
    val category: CraftingBookCategory,
    val showNotification: Boolean,
    val pattern: Array<String>,
    val ingredients: Map<Char, RecipeChoice>,
) : MinecraftRecipe {
    companion object {
        const val EMPTY_INGREDIENT_CHAR = 'X'
    }

    override fun addToManager() {
        pattern.forEachIndexed { i, s -> pattern[i] = s.replace(EMPTY_INGREDIENT_CHAR, ' ') }
        val mojangIngredients = ingredients.mapValues { it.value.toMojangIngredient() }
        val mojangRecipe = MojangShapedRecipe(
            group,
            category.toMojang(),
            ShapedRecipePattern.of(mojangIngredients, *pattern),
            result.toMojangStack(),
            showNotification
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
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
    override val identifier: Identifier,
    override val result: RecipeResult,
    val group: String,
    val category: CraftingBookCategory,
    val ingredients: List<RecipeChoice>,
) : MinecraftRecipe {

    override fun addToManager() {
        val mojangRecipe = MojangShapelessRecipe(
            group,
            category.toMojang(),
            result.toMojangStack(),
            ingredients.map { it.toMojangIngredient() }
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("ingredients", ingredients),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 锻造台转化配方.
 */
class SmithingTransformRecipe(
    override val identifier: Identifier,
    override val result: RecipeResult,
    val base: RecipeChoice,
    val addition: RecipeChoice,
    val template: RecipeChoice,
    val copyDataComponents: Boolean,
) : MinecraftRecipe {

    override fun addToManager() {
        val optionalTemplateIngredient = if (template is EmptyRecipeChoice) {
            Optional.empty()
        } else {
            Optional.of(template.toMojangIngredient())
        }
        val optionalAdditionIngredient = if (addition is EmptyRecipeChoice) {
            Optional.empty()
        } else {
            Optional.of(addition.toMojangIngredient())
        }
        val resultMojangStack = result.toMojangStack()
        val mojangRecipe = MojangSmithingTransformRecipe(
            optionalTemplateIngredient,
            base.toMojangIngredient(),
            optionalAdditionIngredient,
            TransmuteResult(resultMojangStack.itemHolder, resultMojangStack.count, resultMojangStack.componentsPatch),
            copyDataComponents
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
        ExaminableProperty.of("recipe_result", result),
        ExaminableProperty.of("base", base),
        ExaminableProperty.of("addition", addition),
        ExaminableProperty.of("template", template),
        ExaminableProperty.of("copy_data_components", copyDataComponents)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 锻造台纹饰配方.
 */
class SmithingTrimRecipe(
    override val identifier: Identifier,
    val base: RecipeChoice,
    val addition: RecipeChoice,
    val template: RecipeChoice,
    val trimPattern: Identifier,
    val copyDataComponents: Boolean,
) : MinecraftRecipe {
    // 锻造台纹饰配方的结果是原版生成的
    override val result: RecipeResult = EmptyRecipeResult

    override fun addToManager() {
        val mojangRecipe = MojangSmithingTrimRecipe(
            template.toMojangIngredient(),
            base.toMojangIngredient(),
            addition.toMojangIngredient(),
            trimPattern.getTrimPattern().toMojangHolder(),
            copyDataComponents
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
        ExaminableProperty.of("base", base),
        ExaminableProperty.of("addition", addition),
        ExaminableProperty.of("template", template),
        ExaminableProperty.of("copy_data_components", copyDataComponents)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 烟熏炉配方.
 */
class SmokingRecipe(
    override val identifier: Identifier,
    override val result: RecipeResult,
    val group: String,
    val category: CookingBookCategory,
    val input: RecipeChoice,
    val cookingTime: Int,
    val exp: Float,
) : MinecraftRecipe {
    override fun addToManager() {
        val mojangRecipe = MojangSmokingRecipe(
            group,
            category.toMojang(),
            input.toMojangIngredient(),
            result.toMojangStack(),
            exp,
            cookingTime
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
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
    override val identifier: Identifier,
    override val result: RecipeResult,
    val group: String,
    val input: RecipeChoice,
) : MinecraftRecipe {
    override fun addToManager() {
        val mojangRecipe = MojangStonecuttingRecipe(
            group,
            input.toMojangIngredient(),
            result.toMojangStack()
        )
        val recipeHolder = RecipeHolder(identifier.createResourceKey(), mojangRecipe)
        RECIPE_MANAGER.addRecipe(recipeHolder)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
        ExaminableProperty.of("result", result),
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
        val group = node.node("group").getString("")
        val category = node.node("category").get<CookingBookCategory>(CookingBookCategory.MISC)
        val key = node.getRecipeKey()

        BlastingRecipe(
            identifier = key,
            result = result,
            group = group,
            category = category,
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
        val group = node.node("group").getString("")
        val category = node.node("category").get<CookingBookCategory>(CookingBookCategory.MISC)
        val key = node.getRecipeKey()

        CampfireRecipe(
            identifier = key,
            result = result,
            group = group,
            category = category,
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
        val group = node.node("group").getString("")
        val category = node.node("category").get<CookingBookCategory>(CookingBookCategory.MISC)
        val key = node.getRecipeKey()

        FurnaceRecipe(
            identifier = key,
            result = result,
            group = group,
            category = category,
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

        // 判断特殊字符是否被误用
        require(!ingredients.keys.contains(ShapedRecipe.EMPTY_INGREDIENT_CHAR)) { "${ShapedRecipe.EMPTY_INGREDIENT_CHAR} means an empty slot in pattern, should not be used as an ingredient char" }

        // 判断 pattern 中的 key 是否都在 ingredients 中
        val missingIngredientKeys = pattern
            .map { it.toCharArray() }
            .reduce { acc, chars -> acc + chars }
            .distinct()
            .filter { it !in ingredients.keys && it != ShapedRecipe.EMPTY_INGREDIENT_CHAR }
        if (missingIngredientKeys.isNotEmpty()) {
            throw SerializationException("The keys [${missingIngredientKeys.joinToString(prefix = "'", postfix = "'")}] are specified in the pattern but not present in the ingredients")
        }

        val result = node.node("result").require<RecipeResult>()
        val group = node.node("group").getString("")
        val category = node.node("category").get<CraftingBookCategory>(CraftingBookCategory.MISC)
        val showNotification = node.node("show_notification").getBoolean(true)
        val key = node.getRecipeKey()

        ShapedRecipe(
            identifier = key,
            result = result,
            group = group,
            category = category,
            showNotification = showNotification,
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
        val group = node.node("group").getString("")
        val category = node.node("category").get<CraftingBookCategory>(CraftingBookCategory.MISC)
        val key = node.getRecipeKey()

        ShapelessRecipe(
            identifier = key,
            result = result,
            group = group,
            category = category,
            ingredients = ingredients
        )
    }),
    SMITHING_TRANSFORM(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val base = node.node("base").require<RecipeChoice>()
        val addition = node.node("addition").get<RecipeChoice>(EmptyRecipeChoice)
        val template = node.node("template").get<RecipeChoice>(EmptyRecipeChoice)
        val copyDataComponents = node.node("copy_data_components").getBoolean(true)

        // addition和template不能同时是EmptyRecipeChoice
        require(addition != EmptyRecipeChoice || template != EmptyRecipeChoice) {
            "Addition and template cannot be empty at the same time"
        }

        val result = node.node("result").require<RecipeResult>()
        val key = node.getRecipeKey()

        SmithingTransformRecipe(
            identifier = key,
            result = result,
            base = base,
            addition = addition,
            template = template,
            copyDataComponents = copyDataComponents
        )
    }),
    SMITHING_TRIM(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val base = node.node("base").require<RecipeChoice>()
        val addition = node.node("addition").get<RecipeChoice>(EmptyRecipeChoice)
        val template = node.node("template").get<RecipeChoice>(EmptyRecipeChoice)
        val trimPattern = node.node("pattern").require<Identifier>()
        val copyDataComponents = node.node("copy_data_components").getBoolean(true)

        // addition和template不能同时是EmptyRecipeChoice
        require(addition != EmptyRecipeChoice || template != EmptyRecipeChoice) {
            "`addition` and `template` cannot be empty at the same time"
        }

        val key = node.getRecipeKey()

        SmithingTrimRecipe(
            identifier = key,
            base = base,
            addition = addition,
            template = template,
            trimPattern = trimPattern,
            copyDataComponents = copyDataComponents
        )
    }),
    SMOKING(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val input = node.node("input").require<RecipeChoice>()
        val cookingTime = node.node("cooking_time").getInt(100)
        val exp = node.node("exp").getFloat(0F)

        val result = node.node("result").require<RecipeResult>()
        val group = node.node("group").getString("")
        val category = node.node("category").get<CookingBookCategory>(CookingBookCategory.MISC)
        val key = node.getRecipeKey()

        SmokingRecipe(
            identifier = key,
            result = result,
            group = group,
            category = category,
            input = input,
            cookingTime = cookingTime,
            exp = exp
        )
    }),
    STONECUTTING(RecipeTypeBridge(typeTokenOf()) { _, node ->
        val input = node.node("input").require<RecipeChoice>()

        val result = node.node("result").require<RecipeResult>()
        val group = node.node("group").getString("")
        val key = node.getRecipeKey()

        StonecuttingRecipe(
            identifier = key,
            result = result,
            group = group,
            input = input,
        )
    });

    fun deserialize(node: ConfigurationNode): MinecraftRecipe? {
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
internal class RecipeTypeBridge<T : MinecraftRecipe>(
    val typeToken: TypeToken<T>,
    val serializer: SimpleSerializer<T>,
) {
    constructor(
        typeToken: TypeToken<T>,
        serializer: (Type, ConfigurationNode) -> T,
    ) : this(
        typeToken,
        SimpleSerializer<T> { type, node -> serializer(type, node) }
    )
}

/**
 * 方便函数.
 */
private fun Identifier.createResourceKey(): MojangResourceKey<Recipe<*>> {
    return MojangResourceKey.create(Registries.RECIPE, this.toResourceLocation())
}

/**
 * 方便函数.
 */
private fun Identifier.getTrimPattern(): TrimPattern {
    val trimPattern = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(this)
    if (trimPattern == null) {
        throw NoSuchElementException("Trim pattern does not exist: $trimPattern")
    } else {
        return trimPattern
    }
}

/**
 * 方便函数.
 */
private fun CraftingBookCategory.toMojang(): MojangCraftingBookCategory {
    return CraftRecipe.getCategory(this)
}

/**
 * 方便函数.
 */
private fun CookingBookCategory.toMojang(): MojangCookingBookCategory {
    return CraftRecipe.getCategory(this)
}

/**
 * 方便函数.
 */
private fun TrimPattern.toMojangHolder(): Holder<MojangTrimPattern> {
    return CraftTrimPattern.bukkitToMinecraftHolder(this)
}