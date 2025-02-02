package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.core.ItemXSerializer
import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.craftingstation.recipe.StationChoiceSerializer
import cc.mewcraft.wakame.craftingstation.recipe.StationRecipeSerializer
import cc.mewcraft.wakame.craftingstation.recipe.StationResultSerializer
import cc.mewcraft.wakame.item.ItemRegistryConfigStorage
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting

@Init(
    stage = InitStage.POST_WORLD,
)
@Reload(
    runAfter = [ItemRegistryConfigStorage::class],
)
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

                val recipeNode = buildYamlConfigLoader {
                    withDefaults()
                    serializers {
                        register(StationRecipeSerializer)
                        register(StationChoiceSerializer)
                        register(StationResultSerializer)
                        register(ItemXSerializer)
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(StationRecipeSerializer.HINT_NODE, key)
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
            if (recipe.valid()) {
                recipes[key] = recipe
            } else {
                LOGGER.warn("Can't register station recipe: '$key'")
            }
        }

        LOGGER.info("Registered station recipes: {}", recipes.keys.joinToString())
        LOGGER.info("Registered ${recipes.size} station recipes")
    }
}