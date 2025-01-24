package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.ObjectMappers
import cc.mewcraft.wakame.craftingstation.CraftingStationRecipeRegistry
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.recipe.VanillaRecipeRegistry
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
    runBefore = [VanillaRecipeRegistry::class, CraftingStationRecipeRegistry::class]
)
@ReloadDependency(
    runBefore = [VanillaRecipeRegistry::class, CraftingStationRecipeRegistry::class]
)
object ItemCatalogManager : Initializable, KoinComponent {
    private const val CATALOG_ITEM_CONFIG_FILE = "catalog/item/config.yml"
    private const val CATEGORY_DIR_NAME = "catalog/item/category"
    private val categories: MutableMap<String, Category> = mutableMapOf()
    private val logger: Logger by inject()

    lateinit var mainMenuSettings: BasicMenuSettings

    fun findCategory(id: String): Category? {
        return categories[id]
    }

    fun getCategoryMap(): Map<String, Category> {
        return categories.toMap()
    }

    fun loadConfig() {
        val root = yamlConfig {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(CATALOG_ITEM_CONFIG_FILE).bufferedReader() }
            serializers {
                registerAnnotatedObjects(ObjectMappers.DEFAULT)
            }
        }.build().load()

        mainMenuSettings = root.node("main_menu_settings").krequire<BasicMenuSettings>()
    }

    fun loadCategories() {
        categories.clear()

        val categoryDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(CATEGORY_DIR_NAME)
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
                            registerAnnotatedObjects(ObjectMappers.DEFAULT)
                            kregister(CategorySerializer)
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