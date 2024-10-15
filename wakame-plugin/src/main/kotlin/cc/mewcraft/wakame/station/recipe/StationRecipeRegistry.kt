package cc.mewcraft.wakame.station.recipe

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.core.ItemXSerializer
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.*
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

@ReloadDependency(
    runBefore = [ItemRegistry::class]
)
internal object StationRecipeRegistry : Initializable, KoinComponent {
    private const val RECIPE_DIR_NAME = "station/recipes"

    @VisibleForTesting
    val raw: MutableMap<Key, StationRecipe> = mutableMapOf()

    private val recipes: MutableMap<Key, StationRecipe> = mutableMapOf()

    fun find(key: Key): StationRecipe? {
        return recipes[key]
    }

    private val logger: Logger by inject()

    @VisibleForTesting
    fun loadConfig() {
        raw.clear()

        val recipeDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(RECIPE_DIR_NAME)
        val namespacedPaths = NamespacedPathCollector(recipeDir, true).collect("yml")
        namespacedPaths.forEach {
            val file = it.file
            try {
                val fileText = file.readText()
                val key = Key.key(it.namespace, it.path)

                val recipeNode = yamlConfig {
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
                val stationRecipe = recipeNode.krequire<StationRecipe>()
                // 添加进临时注册表
                raw[key] = stationRecipe

            } catch (e: Throwable) {
                val message = "Can't load station recipe: '${file.relativeTo(recipeDir)}'"
                if (RunningEnvironment.TEST.isRunning()) {
                    throw IllegalArgumentException(message, e)
                }
                logger.warn(message, e)
            }
        }
    }

    private suspend fun registerRecipes() {
        raw.forEach { (key, recipe) ->
            if (recipe.isValid()) {
                recipes[key] = recipe
            } else {
                logger.warn("Can't register station recipe: '$key'")
            }
        }

        logger.info("Registered station recipes: {}", recipes.keys.joinToString())
        logger.info("Registered ${recipes.size} station recipes")

        PluginEventBus.get().post(StationRecipeLoadEvent)
    }

    override suspend fun onPostWorldAsync() {
        loadConfig()
        registerRecipes()
    }

    override suspend fun onReloadAsync() {
        loadConfig()
        registerRecipes()
    }
}

/**
 * 当合成站所有配方加载完毕时发生.
 */
object StationRecipeLoadEvent
