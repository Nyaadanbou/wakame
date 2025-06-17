package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.craftingstation.recipe.RecipeChoice
import cc.mewcraft.wakame.craftingstation.recipe.RecipeResult
import cc.mewcraft.wakame.item2.ItemRef
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting

@Init(
    stage = InitStage.POST_WORLD,
)
@Reload
internal object CraftingStationRecipeRegistry {

    @VisibleForTesting
    val raw: MutableMap<Key, Recipe> = mutableMapOf()

    private val recipes: MutableMap<Key, Recipe> = mutableMapOf()

    operator fun get(key: Key): Recipe? {
        return recipes[key]
    }

    @InitFun
    fun load() {
        loadDataIntoRegistry()
        registerStationRecipes()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
        registerStationRecipes()
    }

    @VisibleForTesting
    fun loadDataIntoRegistry() {
        raw.clear()

        val recipeDir = KoishDataPaths.CONFIGS
            .resolve(CraftingStationConstants.DATA_DIR)
            .resolve("recipes")
            .toFile()
        for ((file, namespace, path) in NamespacedFileTreeWalker(recipeDir, "yml", true)) {
            try {
                val fileText = file.readText()
                val key = Key.key(namespace, path)

                val recipeNode = yamlLoader {
                    withDefaults()
                    serializers {
                        registerExact(Recipe.Serializer)
                        registerExact(RecipeChoice.Serializer)
                        registerExact(RecipeResult.Serializer)
                        register(ItemRef.SERIALIZER)
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(Recipe.Serializer.HINT_NODE, key)
                // 反序列化 Recipe
                val recipe = recipeNode.require<Recipe>()
                // 添加进临时注册表
                raw[key] = recipe

            } catch (e: Throwable) {
                val message = "Can't load station recipe: '${file.relativeTo(recipeDir)}'"
                Util.pauseInIde(IllegalArgumentException(message, e))
                LOGGER.warn(message, e)
            }
        }
    }

    private fun registerStationRecipes() {
        raw.forEach { (key, recipe) ->
            recipes[key] = recipe
        }

        LOGGER.info("Registered station recipes: {}", recipes.keys.joinToString())
        LOGGER.info("Registered ${recipes.size} station recipes")
    }
}