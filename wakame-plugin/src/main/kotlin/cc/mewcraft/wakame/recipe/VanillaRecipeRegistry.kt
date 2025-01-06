package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.core.ItemXSerializer
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

@Init(stage = InitStage.POST_WORLD)
@Reload(
    runAfter = [ItemRegistry::class],
)
//@ReloadDependency(
//    runBefore = [ItemRegistry::class]
//)
object VanillaRecipeRegistry : KoinComponent {
    private const val RECIPE_DIR_NAME = "recipes"

    @VisibleForTesting
    val raw: MutableMap<Key, VanillaRecipe> = mutableMapOf()

    //TODO map直接对外暴露，不安全
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

    private val logger: Logger by inject()

    @VisibleForTesting
    fun loadRecipes() {
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
                raw[key] = vanillaRecipe

            } catch (e: Throwable) {
                val message = "Can't load vanilla recipe: '${file.relativeTo(recipeDir)}'"
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
        SMITHING_TRIM.clear()
        SMOKING.clear()
        STONECUTTING.clear()
        raw.forEach {
            try {
                register(it.key, it.value)
            } catch (e: Throwable) {
                logger.warn("Can't register vanilla recipe: '${it.key}'", e)
            }
        }

        logger.info("Registered vanilla recipes: {}", ALL.keys.joinToString(transform = Key::asString))
        logger.info("Registered ${ALL.size} vanilla recipes")
    }

    private fun register(key: Key, vanillaRecipe: VanillaRecipe) {
        val success = vanillaRecipe.registerBukkitRecipe()
        if (!success) {
            logger.warn("Can't register vanilla recipe: '$key'")
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

    @InitFun
    private fun onPostWorld() {
        //TODO 待优化写法
        loadRecipes()
        runTask { registerRecipes() }
    }

    @ReloadFun
    private fun onReload() {
        //TODO 待优化写法
        loadRecipes()
        runTask {
            registerRecipes()
            //向所有玩家的客户端发送配方刷新数据包
            Bukkit.updateRecipes()
        }
    }
}