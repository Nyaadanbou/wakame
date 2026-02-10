package cc.mewcraft.wakame.craftingstation.station

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.craftingstation.recipe.RecipeChoice
import cc.mewcraft.wakame.craftingstation.recipe.RecipeResult
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.configurate.yamlLoader
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting

@Init(InitStage.POST_WORLD)
internal object CraftingStationRegistry {

    private val rawRecipes: MutableMap<Key, Recipe> = mutableMapOf()

    private val checkedRecipes: MutableMap<Key, Recipe> = mutableMapOf()

    private val stations: MutableMap<String, CraftingStation> = mutableMapOf()

    /**
     * 获取所有合成站的唯一标识.
     */
    val NAMES: Set<String>
        get() = stations.keys

    @InitFun
    fun init() {
        // 初始化时延迟一点加载, 以便在 CraftEngine 之后
        runTaskLater(20) { ->
            loadRecipesIntoRegistry()
            registerStationRecipes()
            loadStationsIntoRegistry()
        }
    }

    fun reload() {
        loadRecipesIntoRegistry()
        registerStationRecipes()
        loadStationsIntoRegistry()
    }

    fun getRecipe(key: Key): Recipe? {
        return checkedRecipes[key]
    }

    @VisibleForTesting
    fun getRawRecipe(key: Key): Recipe? {
        return rawRecipes[key]
    }

    fun getStation(id: String): CraftingStation? {
        return stations[id]
    }

    @VisibleForTesting
    fun loadRecipesIntoRegistry() {
        rawRecipes.clear()

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
                rawRecipes[key] = recipe

            } catch (e: Throwable) {
                IdePauser.pauseInIde(e)
                LOGGER.warn("Failed to register station recipe: '${file.relativeTo(recipeDir)}'")
            }
        }
    }

    @VisibleForTesting
    fun loadStationsIntoRegistry() {
        stations.clear()

        val stationDir = KoishDataPaths.CONFIGS
            .resolve(CraftingStationConstants.DATA_DIR)
            .resolve("stations")
            .toFile()
        stationDir.walk()
            .drop(1)
            .filter { it.extension == "yml" }
            .forEach { file ->
                try {
                    val fileText = file.readText()
                    val stationId = file.nameWithoutExtension
                    val stationNode = yamlLoader {
                        withDefaults()
                        serializers {
                            register(StationSerializer)
                        }
                    }.buildAndLoadString(fileText)
                    stationNode.hint(StationSerializer.HINT_NODE, stationId)
                    val station = stationNode.require<CraftingStation>()
                    stations[stationId] = station

                } catch (e: Throwable) {
                    IdePauser.pauseInIde(e)
                    LOGGER.error("Failed to register crafting station config: '${file.relativeTo(stationDir)}'")
                }
            }

        LOGGER.info("Registered stations: {}", stations.keys.joinToString())
    }

    private fun registerStationRecipes() {
        rawRecipes.forEach { (key, recipe) ->
            checkedRecipes[key] = recipe
        }

        LOGGER.info("Registered station recipes: {}", checkedRecipes.keys.joinToString())
        LOGGER.info("Registered ${checkedRecipes.size} station recipes")
    }
}