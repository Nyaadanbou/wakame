package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.core.ItemXSerializer
import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.craftingstation.recipe.StationChoiceSerializer
import cc.mewcraft.wakame.craftingstation.recipe.StationRecipeSerializer
import cc.mewcraft.wakame.craftingstation.recipe.StationResultSerializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.NamespacedPathCollector
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.File

@Init(
    stage = InitStage.POST_WORLD,
)
@Reload(
    runAfter = [ItemRegistry::class],
)
internal object CraftingStationRecipeRegistry : KoinComponent {
    private const val RECIPE_DIR_PATH = "station/recipes"

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

        val recipeDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(RECIPE_DIR_PATH)
        val namespacedPaths = NamespacedPathCollector(recipeDir, true).collect("yml")
        namespacedPaths.forEach {
            val file = it.file
            try {
                val fileText = file.readText()
                val key = Key.key(it.namespace, it.path)

                val recipeNode = buildYamlConfigLoader {
                    withDefaults()
                    serializers {
                        kregister(StationRecipeSerializer)
                        kregister(StationChoiceSerializer)
                        kregister(StationResultSerializer)
                        kregister(ItemXSerializer)
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(StationRecipeSerializer.HINT_NODE, key)
                // 反序列化 Recipe
                val recipe = recipeNode.krequire<Recipe>()
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