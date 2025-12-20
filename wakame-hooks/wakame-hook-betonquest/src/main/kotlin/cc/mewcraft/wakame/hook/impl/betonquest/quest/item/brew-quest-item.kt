@file:JvmName("BrewItems")

package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.api.recipe.Recipe
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.brewery.bukkit.brew.BrewAdapter
import net.kyori.adventure.text.Component
import org.betonquest.betonquest.api.QuestException
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.kernel.TypeFactory
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.item.QuestItem
import org.betonquest.betonquest.item.QuestItemSerializer
import org.betonquest.betonquest.item.QuestItemTagAdapterWrapper
import org.betonquest.betonquest.item.QuestItemWrapper
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

private val tbpApi: TheBrewingProjectApi
    get() = TheBrewingProject.getInstance()

class BrewQuestItem(
    private val recipe: Recipe<ItemStack>,
    private val quality: BrewQuality?,
) : QuestItem {

    override fun getName(): Component {
        return Component.text(recipe.recipeName)
    }

    override fun getLore(): List<Component> {
        return emptyList()
    }

    override fun generate(stackSize: Int, profile: Profile?): ItemStack {
        // TODO 2025/12/01:
        //  使用 RecipeResult#newBrewItem 来生成物品堆叠
        //  这需要我们传入更多的参数来指定物品酒的各种属性 (如蒸馏次数)
        // 如果未指定 quality (为 null), 则生成 GOOD 品质的酒酿
        return recipe.getRecipeResult(quality ?: BrewQuality.GOOD).newLorelessItem().apply { amount = stackSize }
    }

    override fun matches(item: ItemStack?): Boolean {
        if (item == null) return false
        val brew = BrewAdapter.fromItem(item).getOrNull() ?: return false
        val recipe = brew.closestRecipe(tbpApi.recipeRegistry).getOrNull() ?: return false

        // 比较 Recipe
        if (this.recipe.recipeName != recipe.recipeName) return false

        // FIXME 2025/12/01: 目前这里拿到的 quality 始终是 EXCELLENT, 而非 Brew 实际的品质
        val quality = brew.quality(recipe).getOrNull() ?: return false

        // 比较 Quality
        if (this.quality != null) {
            return this.quality == quality
        } else {
            return true
        }
    }
}

class BrewQuestItemFactory : TypeFactory<QuestItemWrapper> {

    // format: "brew <recipe_name> <quality>"
    override fun parseInstruction(instruction: Instruction): QuestItemWrapper {
        val recipe = instruction.get<Recipe<ItemStack>> { str -> tbpApi.recipeRegistry.getRecipe(str).getOrNull() }
        val quality = instruction.getValue("quality", Argument.ENUM(BrewQuality::class.java))
        val wrapper = BrewQuestItemWrapper(recipe, quality)
        if (instruction.hasArgument("quest-item")) {
            return QuestItemTagAdapterWrapper(wrapper)
        }
        return wrapper
    }
}

class BrewQuestItemSerializer : QuestItemSerializer {

    override fun serialize(itemStack: ItemStack): String {
        val brew = BrewAdapter.fromItem(itemStack).getOrNull() ?: throw QuestException("ItemStack is not a Brew item")
        val recipe = brew.closestRecipe(tbpApi.recipeRegistry).getOrNull() ?: throw QuestException("Cannot find recipe for the Brew item")
        val quality = brew.quality(recipe).getOrNull() ?: throw QuestException("Cannot determine quality for the Brew item")
        return "${recipe.recipeName} quality:${quality.name.lowercase()}"
    }
}

class BrewQuestItemWrapper(
    private val recipe: Variable<Recipe<ItemStack>>,
    private val quality: Variable<BrewQuality>?,
) : QuestItemWrapper {

    override fun getItem(profile: Profile?): QuestItem? {
        val recipe = this.recipe.getValue(profile)
        val quality = this.quality?.getValue(profile)
        return BrewQuestItem(recipe, quality)
    }
}