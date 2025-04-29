package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.brewery.BrewRecipeManager
import cc.mewcraft.wakame.brewery.BrewRecipeRenderer
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.ItemRefHandler
import cc.mewcraft.wakame.item2.KoishItemRefHandler
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import com.dre.brewery.BarrelWoodType
import com.dre.brewery.api.BreweryApi
import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.recipe.PluginItem
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import cc.mewcraft.wakame.brewery.BarrelWoodType as KBarrelWoodType

@Hook(plugins = ["BreweryX"])
object BreweryXHook {

    init {
        registerItemHandlers()
        registerBarrelWoodTypes()
        registerApiImplementations()
    }

    private fun registerItemHandlers() {
        // 使 BreweryX 可以识别 Koish 物品
        PluginItem.registerForConfig("koish", ::KoishPluginItem)
        // 使 Koish 可以识别 BreweryX 物品
        BuiltInRegistries.ITEM_REF_HANDLER_EXTERNAL.add("brewery", BreweryXItemRefHandler)
    }

    private fun registerApiImplementations() {
        BrewRecipeManager.register(TheBrewRecipeManager)
        BrewRecipeRenderer.register(TheBrewRecipeRenderer)
    }

    private fun registerBarrelWoodTypes() {
        for (type in BarrelWoodType.entries) {
            val formattedName = type.formattedName
            val translatedName = type.formattedName // TODO #383: 支持 i18n
            val id = Identifiers.of(type.name.lowercase())
            val obj = KBarrelWoodType(formattedName, translatedName)
            KBarrelWoodType.REGISTRY.add(id, obj)
        }
    }
}

/**
 * ItemRef formats of Brewery items:
 * - "brewery:example/1" -> id 为 example, 品质为1
 * - "brewery:example/2" -> id 为 example, 品质为2
 * - "brewery:example/3" -> id 为 example, 品质为3
 *
 * 路径中的最后一个斜杠右边的数字将被解析为品质.
 */
object BreweryXItemRefHandler : ItemRefHandler<BRecipe> {

    private const val NAMESPACE = "brewery"

    override val systemName: String = "BreweryX"

    override fun accepts(id: Identifier): Boolean {
        // 这里没有判断 quality 是因为似乎没有特别直接的办法得知一个 quality 是否存在

        if (id.namespace() != NAMESPACE) return false
        val value = id.value()
        val recipeId = value.extractRecipeId() ?: return false
        val recipe = BRecipe.getById(recipeId)
        return recipe != null
    }

    override fun getId(stack: ItemStack): Identifier? {
        val brew = BreweryApi.getBrew(stack) ?: return null
        val recipeId = brew.currentRecipe.id
        val quality = brew.quality
        return Identifiers.of(NAMESPACE, "$recipeId/$quality")
    }

    override fun getName(id: Identifier): Component? {
        if (id.namespace() != NAMESPACE) return null
        val value = id.value()
        val recipeId = value.extractRecipeId() ?: return null
        val quality = value.extractQuality() ?: return null
        val recipe = BRecipe.getById(recipeId) ?: return null
        val recipeName = recipe.getName(quality)
        return Component.text(recipeName)
    }

    override fun getInternalType(id: Identifier): BRecipe? {
        // Brewery API 没办法从 id 拿到一个具有特定 quality 的 BRecipe,
        // 因为 BRecipe 本身就包含了一个 recipe 可能出现的所有 quality.
        // 而不同的 quality 在我们的定义下是属于不同的 ItemRef 的.
        // 所以干脆这里返回 null 来表示我们不使用这个函数.

        return null
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        if (id.namespace() != NAMESPACE) return null
        val value = id.value()
        val recipeId = value.extractRecipeId() ?: return null
        val quality = value.extractQuality() ?: return null
        val ret = BreweryApi.createBrewItem(recipeId, quality, player)
        return ret
    }

    private fun String.extractRecipeId(): String? {
        return substringBeforeLast('/')
    }

    private fun String.extractQuality(): Int? {
        return substringAfterLast('/').toIntOrNull()
    }
}

class KoishPluginItem : PluginItem() {

    override fun matches(p0: ItemStack): Boolean {
        val id = KoishItemRefHandler.getId(p0) ?: return false
        return itemId.equals(id.value(), ignoreCase = true)
    }
}