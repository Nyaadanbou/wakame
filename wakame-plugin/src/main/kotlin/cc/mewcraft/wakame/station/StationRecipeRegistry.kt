package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.recipe.*
import cc.mewcraft.wakame.recipe.RecipeChoiceSerializer
import cc.mewcraft.wakame.recipe.RecipeResultSerializer
import cc.mewcraft.wakame.recipe.VanillaRecipeSerializer
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

@ReloadDependency(
    runBefore = [ItemRegistry::class]
)
object StationRecipeRegistry : Initializable, KoinComponent {
    private const val RECIPE_DIR_NAME = "station/recipes"
    val raw: MutableMap<Key, StationRecipe> = mutableMapOf()

    private val logger: Logger by inject()

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
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(StationRecipeSerializer.HINT_NODE, key)
                // 反序列化 Recipe
                val stationRecipe = recipeNode.krequire<StationRecipe>()
                // 添加进临时注册表
                raw[key] = stationRecipe
                logger.info("Loading station recipe: '${stationRecipe.key}'")

            } catch (e: Throwable) {
                val message = "Can't load station recipe: '${file.relativeTo(recipeDir)}'"
                if (RunningEnvironment.TEST.isRunning()) {
                    throw IllegalArgumentException(message, e)
                }
                logger.warn(message, e)
            }
        }
    }

    override fun onPostWorld() {
        loadConfig()
    }

    override fun onReload() {
        loadConfig()
    }
}