package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.item.ItemRefHandler
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import com.dre.brewery.api.BreweryApi
import com.dre.brewery.recipe.BRecipe
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 实现了 [ItemRefHandler] 以便让 Koish 可以识别 BreweryX 的物品.
 *
 * ### 物品格式
 * - "brewery:example/1" -> id 为 example, 品质为1
 * - "brewery:example/2" -> id 为 example, 品质为2
 * - "brewery:example/3" -> id 为 example, 品质为3
 *
 * 路径中的最后一个斜杠右边的数字将被解析为品质.
 */
object BreweryXItemRefHandler : ItemRefHandler<BRecipe> {

    private const val NAMESPACE = "brewery"

    override val systemName: String = "BreweryX"

    override fun accepts(id: KoishKey): Boolean {
        // 这里没有判断 quality 是因为似乎没有特别直接的办法得知一个 quality 是否存在

        if (id.namespace() != NAMESPACE) return false
        val value = id.value()
        val recipeId = value.extractRecipeId() ?: return false
        val recipe = BRecipe.getById(recipeId)
        return recipe != null
    }

    override fun getId(stack: ItemStack): KoishKey? {
        val brew = BreweryApi.getBrew(stack) ?: return null
        val recipeId = brew.currentRecipe.id
        val quality = brew.quality
        return KoishKeys.of(NAMESPACE, "$recipeId/$quality")
    }

    override fun getName(id: KoishKey): Component? {
        if (id.namespace() != NAMESPACE) return null
        val value = id.value()
        val recipeId = value.extractRecipeId() ?: return null
        val quality = value.extractQuality() ?: return null
        val recipe = BRecipe.getById(recipeId) ?: return null
        val recipeName = recipe.getName(quality)
        return Component.text(recipeName)
    }

    override fun getInternalType(id: KoishKey): BRecipe? {
        // Brewery API 没办法从 id 拿到一个具有特定 quality 的 BRecipe,
        // 因为 BRecipe 本身就包含了一个 recipe 可能出现的所有 quality.
        // 而不同的 quality 在我们的定义下是属于不同的 ItemRef 的.
        // 所以干脆这里返回 null 来表示我们不使用这个函数.

        return null
    }

    override fun createItemStack(id: KoishKey, amount: Int, player: Player?): ItemStack? {
        if (id.namespace() != NAMESPACE) return null
        val value = id.value()
        val recipeId = value.extractRecipeId() ?: return null
        val quality = value.extractQuality() ?: return null
        val ret = BreweryApi.createBrewItem(recipeId, quality, player) ?: return null
        return ret.asQuantity(amount)
    }

    private fun String.extractRecipeId(): String? {
        return substringBeforeLast('/')
    }

    private fun String.extractQuality(): Int? {
        return substringAfterLast('/').toIntOrNull()
    }
}