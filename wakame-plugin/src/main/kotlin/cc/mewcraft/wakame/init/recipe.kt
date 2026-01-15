package cc.mewcraft.wakame.init

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.event.map.MinecraftRecipeRegistrationDoneEvent
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.recipe.*
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.configurate.yamlLoader
import cc.mewcraft.wakame.util.data.isYaml
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import cc.mewcraft.wakame.util.runTask
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.jetbrains.annotations.VisibleForTesting
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.walk


@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [ItemTagInitializer::class]
)
object RecipeInitializer {

    private val recipeDirectory: Path = KoishDataPaths.CONFIGS.resolve("recipe")
    private val ymlLoaderBuilder: YamlConfigurationLoader.Builder = yamlLoader {
        withDefaults()
        serializers {
            register<MinecraftRecipe>(MinecraftRecipe.Serializer)
            register<RecipeChoice>(RecipeChoiceSerializer)
            register<RecipeResult>(RecipeResultSerializer)
            register(ItemRef.SERIALIZER)
        }
    }

    @VisibleForTesting
    val uncheckedRecipes: HashMap<Key, MinecraftRecipe> = HashMap(512)
    @VisibleForTesting
    val checkedRecipes: HashMap<Key, MinecraftRecipe> = HashMap(512)

    @InitFun
    fun init() {
        preLoad()
        runTask(::postLoad)
    }

    fun reload() {
        preLoad()
        postLoad()
    }

    @VisibleForTesting
    fun preLoad() {
        uncheckedRecipes.clear()
        recipeDirectory.walk()
            .filter(Path::isYaml)
            .forEach(this::loadKoishRecipe)
    }

    @VisibleForTesting
    fun postLoad() {
        // 将从 Koish 读取到的配方注册到 NMS
        registerVanillaRecipes()
        // 向所有客户端发送新的配方数据包
        Bukkit.updateRecipes()
        // 通知其他系统
        MapEventBus.post(MinecraftRecipeRegistrationDoneEvent)
    }

    private fun loadKoishRecipe(path: Path) {
        try {
            val rootNode = ymlLoaderBuilder.buildAndLoadString(path.readText())
            val recipeId = Identifiers.ofKoish(path.relativeTo(recipeDirectory).toString())
            // 注入 key 节点
            rootNode.hint(RepresentationHints.MINECRAFT_RECIPE_ID, recipeId)
            // 反序列化 Recipe
            val minecraftRecipe = rootNode.require<MinecraftRecipe>()
            // 添加进临时注册表
            uncheckedRecipes[recipeId] = minecraftRecipe

        } catch (e: Throwable) {
            IdePauser.pauseInIde(IllegalStateException("Can't load vanilla recipe: '${path.relativeTo(recipeDirectory)}'", e))
        }
    }

    private fun registerVanillaRecipes() {
        checkedRecipes.values.forEach(MinecraftRecipe::removeFromManager)
        checkedRecipes.clear()
        uncheckedRecipes.forEach { (key, recipe) ->
            try {
                recipe.addToManager()
                checkedRecipes[key] = recipe
            } catch (e: Throwable) {
                IdePauser.pauseInIde(IllegalStateException("Can't register vanilla recipe: '$key'", e))
            }
        }

        LOGGER.info("Registered vanilla recipes: {}", checkedRecipes.keys.joinToString(transform = Key::asString))
        LOGGER.info("Registered ${checkedRecipes.size} vanilla recipes")
    }
}