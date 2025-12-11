package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.event.map.MinecraftRecipeRegistrationDoneEvent
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.ItemTagManager
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.jetbrains.annotations.VisibleForTesting
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        ItemTagManager::class // 需要在标签加载后
    ]
)
@Reload(
    runAfter = [
        ItemTagManager::class // 需要在标签加载后
    ]
)
internal object MinecraftRecipeRegistryLoader {
    const val DATA_DIR = "recipe"

    @VisibleForTesting
    val uncheckedRecipes: HashMap<Key, MinecraftRecipe> = HashMap(512)
    private val checkedRecipes: HashMap<Key, MinecraftRecipe> = HashMap(512)

    @InitFun
    fun init() {
        load()
        runTask(::postLoad)
    }

    @ReloadFun
    fun reload() {
        load()
        runTask(::postLoad)
    }

    private fun postLoad() {
        // 将从 Koish 读取到的配方注册到 Minecraft
        registerRecipes()
        // 向所有客户端发送新的配方数据包
        Bukkit.updateRecipes()
        // 通知其他系统
        MapEventBus.post(MinecraftRecipeRegistrationDoneEvent)
    }

    @VisibleForTesting
    fun load() {
        uncheckedRecipes.clear()

        val recipeDir = KoishDataPaths.CONFIGS.resolve(DATA_DIR)
        recipeDir.walk()
            .filter { it.extension == "yml" }
            .forEach { path ->
                try {
                    val rootNode = yamlLoader {
                        withDefaults()
                        serializers {
                            register<MinecraftRecipe>(MinecraftRecipe.Serializer)
                            register<RecipeChoice>(RecipeChoiceSerializer)
                            register<RecipeResult>(RecipeResultSerializer)
                            register(ItemRef.SERIALIZER)
                        }
                    }.buildAndLoadString(path.readText())

                    val recipeId = Identifiers.ofKoish(path.relativeTo(recipeDir).toString())
                    // 注入 key 节点
                    rootNode.hint(RepresentationHints.MINECRAFT_RECIPE_ID, recipeId)
                    // 反序列化 Recipe
                    val minecraftRecipe = rootNode.require<MinecraftRecipe>()
                    // 添加进临时注册表
                    uncheckedRecipes[recipeId] = minecraftRecipe

                } catch (e: Throwable) {
                    Util.pauseInIde(IllegalStateException("Can't load vanilla recipe: '${path.relativeTo(recipeDir)}'", e))
                }
            }
    }

    private fun registerRecipes() {
        checkedRecipes.values.forEach(MinecraftRecipe::removeFromManager)
        checkedRecipes.clear()
        uncheckedRecipes.forEach { (key, recipe) ->
            try {
                recipe.addToManager()
                checkedRecipes[key] = recipe
            } catch (e: Throwable) {
                Util.pauseInIde(IllegalStateException("Can't register vanilla recipe: '$key'", e))
            }
        }

        LOGGER.info("Registered vanilla recipes: {}", checkedRecipes.keys.joinToString(transform = Key::asString))
        LOGGER.info("Registered ${checkedRecipes.size} vanilla recipes")
    }
}