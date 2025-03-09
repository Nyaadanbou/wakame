package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.core.ItemXSerializer
import cc.mewcraft.wakame.event.map.MinecraftRecipeRegistrationDoneEvent
import cc.mewcraft.wakame.item.ItemTypeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.jetbrains.annotations.VisibleForTesting

@Init(
    stage = InitStage.POST_WORLD
)
@Reload(
    runAfter = [
        ItemTypeRegistryLoader::class, // deps: 需要直接的数据
    ],
)
object MinecraftRecipeRegistryDataLoader {

    @VisibleForTesting
    val uncheckedRecipes: HashMap<Key, VanillaRecipe> = HashMap(512)
    private val checkedRecipes: HashMap<Key, VanillaRecipe> = HashMap(512)

    @InitFun
    fun init() {
        load()
        runTask(::postLoad)
    }

    @InitFun
    fun reload() {
        load()
        runTask(::postLoad)
    }

    private fun postLoad() {
        // 将从 Koish 读取到的配方注册到 Minecraft
        registerForBukkitRecipes()
        // 向所有客户端发送新的配方数据包
        Bukkit.updateRecipes()
        // 通知其他系统
        MapEventBus.post(MinecraftRecipeRegistrationDoneEvent)
    }

    @VisibleForTesting
    fun load() {
        uncheckedRecipes.clear()

        val recipeDir = KoishDataPaths.CONFIGS.resolve(VanillaRecipeConstants.DATA_DIR).toFile()
        for ((file, namespace, path) in NamespacedFileTreeWalker(recipeDir, "yml", true)) {
            try {
                val fileText = file.readText()
                val key = Key.key(namespace, path)

                val recipeNode = buildYamlConfigLoader {
                    withDefaults()
                    serializers {
                        register<VanillaRecipe>(VanillaRecipeSerializer)
                        register<RecipeChoice>(RecipeChoiceSerializer)
                        register<RecipeResult>(RecipeResultSerializer)
                        register(ItemXSerializer)
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(RepresentationHints.MINECRAFT_RECIPE_ID, key)
                // 反序列化 Recipe
                val vanillaRecipe = recipeNode.require<VanillaRecipe>()
                // 添加进临时注册表
                uncheckedRecipes[key] = vanillaRecipe

            } catch (e: Throwable) {
                Util.pauseInIde(IllegalStateException("Can't load vanilla recipe: '${file.relativeTo(recipeDir)}'", e))
            }
        }
    }

    private fun registerForBukkitRecipes() {
        checkedRecipes.values.forEach(VanillaRecipe::unregisterBukkitRecipe)
        checkedRecipes.clear()
        uncheckedRecipes.forEach {
            try {
                registerForBukkitRecipe0(it.key, it.value)
            } catch (e: Throwable) {
                Util.pauseInIde(IllegalStateException("Can't register vanilla recipe: '${it.key}'", e))
            }
        }

        LOGGER.info("Registered vanilla recipes: {}", checkedRecipes.keys.joinToString(transform = Key::asString))
        LOGGER.info("Registered ${checkedRecipes.size} vanilla recipes")
    }

    private fun registerForBukkitRecipe0(key: Key, vanillaRecipe: VanillaRecipe) {
        val success = vanillaRecipe.registerBukkitRecipe()
        if (!success) {
            LOGGER.warn("Can't register vanilla recipe: '$key'")
            return
        }

        checkedRecipes[key] = vanillaRecipe
    }
}