package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.SimpleRegistry
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

@ReloadDependency(
    runBefore = [ItemRegistry::class]
)
object RecipeRegistry : Initializable, KoinComponent {
    const val RECIPE_DIR_NAME = "recipes"

    val ALL: Registry<Key, Recipe> = SimpleRegistry()
//    val BLASTING: Registry<Key, BlastingRecipe> = SimpleRegistry()
//    val CAMPFIRE: Registry<Key, CampfireRecipe> = SimpleRegistry()
//    val FURNACE: Registry<Key, FurnaceRecipe> = SimpleRegistry()
//    val SHAPED: Registry<Key, ShapedRecipe> = SimpleRegistry()
//    val SHAPELESS: Registry<Key, ShapelessRecipe> = SimpleRegistry()
//    val SMITHING_TRANSFORM: Registry<Key, SmithingTransformRecipe> = SimpleRegistry()
//    val SMOKING: Registry<Key, SmokingRecipe> = SimpleRegistry()
//    val STONECUTTING: Registry<Key, StonecuttingRecipe> = SimpleRegistry()
    //TODO 新的注册表数据结构

    private val logger: Logger by inject()

    private fun loadConfig() {
        ALL.clear()

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
                        kregister(RecipeSerializer)
                        kregister(RecipeChoiceSerializer)
                        kregister(RecipeResultSerializer)
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(RecipeSerializer.HINT_NODE, key)
                // 反序列化 Recipe
                val recipe = recipeNode.krequire<Recipe>()
                // 添加进注册表
                ALL.register(key, recipe)
                logger.info("register recipe: ${recipe.key}")

            } catch (e: Throwable) {
                val message = "Can't load recipe: ${file.relativeTo(recipeDir)}"
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
        //向所有玩家的客户端发送配方刷新数据包
        Bukkit.updateRecipes()
    }
}