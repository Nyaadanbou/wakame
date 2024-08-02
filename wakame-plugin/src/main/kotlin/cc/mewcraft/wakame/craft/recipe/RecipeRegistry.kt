package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
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
    val raw: MutableMap<Key, Recipe> = mutableMapOf()

    val ALL: MutableMap<Key, Recipe> = mutableMapOf()
    val BLASTING: MutableMap<Key, BlastingRecipe> = mutableMapOf()
    val CAMPFIRE: MutableMap<Key, CampfireRecipe> = mutableMapOf()
    val FURNACE: MutableMap<Key, FurnaceRecipe> = mutableMapOf()
    val SHAPED: MutableMap<Key, ShapedRecipe> = mutableMapOf()
    val SHAPELESS: MutableMap<Key, ShapelessRecipe> = mutableMapOf()
    val SMITHING_TRANSFORM: MutableMap<Key, SmithingTransformRecipe> = mutableMapOf()
    val SMOKING: MutableMap<Key, SmokingRecipe> = mutableMapOf()
    val STONECUTTING: MutableMap<Key, StonecuttingRecipe> = mutableMapOf()

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
                        kregister(RecipeSerializer)
                        kregister(RecipeChoiceSerializer)
                        kregister(RecipeResultSerializer)
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(RecipeSerializer.HINT_NODE, key)
                // 反序列化 Recipe
                val recipe = recipeNode.krequire<Recipe>()
                // 添加进临时注册表
                raw[key] = recipe
                logger.info("Loading recipe: '${recipe.key}'")

            } catch (e: Throwable) {
                val message = "Can't load recipe: '${file.relativeTo(recipeDir)}'"
                if (RunningEnvironment.TEST.isRunning()) {
                    throw IllegalArgumentException(message, e)
                }
                logger.warn(message, e)
            }
        }
    }

    private fun registerRecipes() {
        ALL.forEach {
            it.value.unregisterBukkitRecipe()
        }
        ALL.clear()
        BLASTING.clear()
        CAMPFIRE.clear()
        FURNACE.clear()
        SHAPED.clear()
        SHAPELESS.clear()
        SMITHING_TRANSFORM.clear()
        SMOKING.clear()
        STONECUTTING.clear()
        raw.forEach {
            try {
                register(it.key, it.value)
            } catch (e: Throwable) {
                logger.warn("Can't register recipe: '${it.key}'", e)
            }
        }
    }

    private fun register(key: Key, recipe: Recipe) {
        val success = recipe.registerBukkitRecipe()
        if (!success) {
            logger.warn("Can't register recipe: '$key'")
            return
        }
        ALL[key] = recipe
        when (recipe) {
            is BlastingRecipe -> {
                BLASTING[key] = recipe
            }

            is CampfireRecipe -> {
                CAMPFIRE[key] = recipe
            }

            is FurnaceRecipe -> {
                FURNACE[key] = recipe
            }

            is ShapedRecipe -> {
                SHAPED[key] = recipe
            }

            is ShapelessRecipe -> {
                SHAPELESS[key] = recipe
            }

            is SmithingTransformRecipe -> {
                SMITHING_TRANSFORM[key] = recipe
            }

            is SmokingRecipe -> {
                SMOKING[key] = recipe
            }

            is StonecuttingRecipe -> {
                STONECUTTING[key] = recipe
            }
        }
        logger.info("Registered recipe: '${recipe.key}'")
    }

    override fun onPostWorld() {
        //TODO 待优化写法
        loadConfig()
        ThreadType.SYNC.launch {
            registerRecipes()
        }
    }

    override fun onReload() {
        //TODO 待优化写法
        loadConfig()
        ThreadType.SYNC.launch {
            registerRecipes()
        }
        //向所有玩家的客户端发送配方刷新数据包
        Bukkit.updateRecipes()
    }
}