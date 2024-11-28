package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.core.ItemXSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.NamespacedPathCollector
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.File

@ReloadDependency(
    runBefore = [ItemRegistry::class]
)
internal object CraftingStationRecipeRegistry : Initializable, KoinComponent {
    private const val RECIPE_DIR_NAME = "station/recipes"

    @VisibleForTesting
    val raw: MutableMap<Key, Recipe> = mutableMapOf()

    private val recipes: MutableMap<Key, Recipe> = mutableMapOf()

    operator fun get(key: Key): Recipe? {
        return recipes[key]
    }

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
                val recipe = recipeNode.krequire<Recipe>()
                // 添加进临时注册表
                raw[key] = recipe

            } catch (e: Throwable) {
                val message = "Can't load station recipe: '${file.relativeTo(recipeDir)}'"
                if (RunningEnvironment.TEST.isRunning()) {
                    throw IllegalArgumentException(message, e)
                }
                LOGGER.warn(message, e)
            }
        }
    }

    private fun registerRecipes() {
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

    override fun onPostWorld() {
        loadConfig()
        registerRecipes()
    }

    override fun onReload() {
        loadConfig()
        registerRecipes()
    }
}
