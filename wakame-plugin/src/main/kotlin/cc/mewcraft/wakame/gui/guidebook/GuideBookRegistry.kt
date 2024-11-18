package cc.mewcraft.wakame.gui.guidebook

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.gui.MenuLayoutSerializer
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.recipe.VanillaRecipeRegistry
import cc.mewcraft.wakame.station.recipe.StationRecipeRegistry
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

@PostWorldDependency(
    runBefore = [VanillaRecipeRegistry::class, StationRecipeRegistry::class]
)
@ReloadDependency(
    runBefore = [VanillaRecipeRegistry::class, StationRecipeRegistry::class]
)
object GuideBookRegistry : Initializable, KoinComponent {
    private const val GUIDEBOOK_GLOBAL_CONFIG_FILE = "guidebook/config.yml"
    private const val CATEGORIES_DIR_NAME = "guidebook/categories"
    private val categories: MutableMap<String, Category> = mutableMapOf()
    private val logger: Logger by inject()
    lateinit var mainMenuLayout: MenuLayout
    lateinit var categoryMenuLayout: MenuLayout


    fun loadConfig() {
        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(GUIDEBOOK_GLOBAL_CONFIG_FILE).bufferedReader() }
            serializers {
                withDefaults()
                kregister(MenuLayoutSerializer)
            }
        }.build().load()

        mainMenuLayout = root.node("main_menu_layout").krequire<MenuLayout>()
        categoryMenuLayout = root.node("category_menu_layout").krequire<MenuLayout>()
    }

    fun loadCategories() {
        categories.clear()

        val categoryDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(CATEGORIES_DIR_NAME)
        categoryDir.walk()
            .drop(1)
            .filter { it.extension == "yml" }
            .forEach { file ->
                try {
                    val fileText = file.readText()
                    val id = file.nameWithoutExtension
                    val categoryNode = yamlConfig {
                        withDefaults()
                        serializers {
                            kregister(CategorySerializer)
                            kregister(MenuLayoutSerializer)
                        }
                    }.buildAndLoadString(fileText)
                    categoryNode.hint(CategorySerializer.HINT_NODE, id)
                    val category = categoryNode.krequire<Category>()
                    categories[id] = category

                } catch (e: Throwable) {
                    val message = "Can't register guidebook item category: '${file.relativeTo(categoryDir)}'"
                    if (RunningEnvironment.TEST.isRunning()) {
                        throw IllegalArgumentException(message, e)
                    }
                    logger.warn(message, e)
                }
            }

        logger.info("Registered guidebook item categories: {}", categories.keys.joinToString())
    }

    override fun onPostWorld() {
        loadConfig()
        loadCategories()
    }

    override fun onReload() {
        loadConfig()
        loadCategories()
    }
}