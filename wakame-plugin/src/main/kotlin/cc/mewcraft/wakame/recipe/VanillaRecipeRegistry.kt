package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.core.ItemXSerializer
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
import cc.mewcraft.wakame.util.runTask
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.qualifier.named
import java.io.File

@Init(
    stage = InitStage.POST_WORLD
)
@Reload(
    runAfter = [ItemRegistry::class],
)
object VanillaRecipeRegistry {
    private const val RECIPE_DIR_PATH = "recipes"

    @VisibleForTesting
    val RAW: MutableMap<Key, VanillaRecipe> = mutableMapOf()

    // TODO 无需在这分类储存, 这部分数据分类应该由需要分类的系统自己实现 (例如图鉴)
    val ALL: MutableMap<Key, VanillaRecipe> = mutableMapOf()
    val BLASTING: MutableMap<Key, BlastingRecipe> = mutableMapOf()
    val CAMPFIRE: MutableMap<Key, CampfireRecipe> = mutableMapOf()
    val FURNACE: MutableMap<Key, FurnaceRecipe> = mutableMapOf()
    val SHAPED: MutableMap<Key, ShapedRecipe> = mutableMapOf()
    val SHAPELESS: MutableMap<Key, ShapelessRecipe> = mutableMapOf()
    val SMITHING_TRANSFORM: MutableMap<Key, SmithingTransformRecipe> = mutableMapOf()
    val SMITHING_TRIM: MutableMap<Key, SmithingTrimRecipe> = mutableMapOf()
    val SMOKING: MutableMap<Key, SmokingRecipe> = mutableMapOf()
    val STONECUTTING: MutableMap<Key, StonecuttingRecipe> = mutableMapOf()

    @InitFun
    private fun init() {
        loadDataIntoRegistry()
        runTask {
            registerForBukkitRecipes()
            Bukkit.updateRecipes() // 向所有客户端发送新的配方数据包
        }
    }

    @ReloadFun
    private fun reload() {
        loadDataIntoRegistry()
        runTask {
            registerForBukkitRecipes()
            Bukkit.updateRecipes() // 向所有客户端发送新的配方数据包
        }
    }

    @VisibleForTesting
    fun loadDataIntoRegistry() {
        RAW.clear()

        val recipeDir = Injector.get<File>(named(PLUGIN_DATA_DIR)).resolve(RECIPE_DIR_PATH)
        val namespacedPaths = NamespacedPathCollector(recipeDir, true).collect("yml")
        namespacedPaths.forEach {
            val file = it.file
            try {
                val fileText = file.readText()
                val key = Key.key(it.namespace, it.path)

                val recipeNode = buildYamlConfigLoader {
                    withDefaults()
                    serializers {
                        kregister(VanillaRecipeSerializer)
                        kregister(RecipeChoiceSerializer)
                        kregister(RecipeResultSerializer)
                        kregister(ItemXSerializer)
                    }
                }.buildAndLoadString(fileText)

                // 注入 key 节点
                recipeNode.hint(VanillaRecipeSerializer.HINT_NODE, key)
                // 反序列化 Recipe
                val vanillaRecipe = recipeNode.krequire<VanillaRecipe>()
                // 添加进临时注册表
                RAW[key] = vanillaRecipe

            } catch (e: Throwable) {
                Util.pauseInIde(IllegalStateException("Can't load vanilla recipe: '${file.relativeTo(recipeDir)}'", e))
            }
        }
    }

    private fun registerForBukkitRecipes() {
        ALL.values.forEach(VanillaRecipe::unregisterBukkitRecipe)
        ALL.clear()
        BLASTING.clear()
        CAMPFIRE.clear()
        FURNACE.clear()
        SHAPED.clear()
        SHAPELESS.clear()
        SMITHING_TRANSFORM.clear()
        SMITHING_TRIM.clear()
        SMOKING.clear()
        STONECUTTING.clear()
        RAW.forEach {
            try {
                registerForBukkitRecipe0(it.key, it.value)
            } catch (e: Throwable) {
                Util.pauseInIde(IllegalStateException("Can't register vanilla recipe: '${it.key}'", e))
            }
        }

        LOGGER.info("Registered vanilla recipes: {}", ALL.keys.joinToString(transform = Key::asString))
        LOGGER.info("Registered ${ALL.size} vanilla recipes")
    }

    private fun registerForBukkitRecipe0(key: Key, vanillaRecipe: VanillaRecipe) {
        val success = vanillaRecipe.registerBukkitRecipe()
        if (!success) {
            LOGGER.warn("Can't register vanilla recipe: '$key'")
            return
        }

        ALL[key] = vanillaRecipe
        when (vanillaRecipe) {
            is BlastingRecipe -> {
                BLASTING[key] = vanillaRecipe
            }

            is CampfireRecipe -> {
                CAMPFIRE[key] = vanillaRecipe
            }

            is FurnaceRecipe -> {
                FURNACE[key] = vanillaRecipe
            }

            is ShapedRecipe -> {
                SHAPED[key] = vanillaRecipe
            }

            is ShapelessRecipe -> {
                SHAPELESS[key] = vanillaRecipe
            }

            is SmithingTransformRecipe -> {
                SMITHING_TRANSFORM[key] = vanillaRecipe
            }

            is SmithingTrimRecipe -> {
                SMITHING_TRIM[key] = vanillaRecipe
            }

            is SmokingRecipe -> {
                SMOKING[key] = vanillaRecipe
            }

            is StonecuttingRecipe -> {
                STONECUTTING[key] = vanillaRecipe
            }
        }
    }
}