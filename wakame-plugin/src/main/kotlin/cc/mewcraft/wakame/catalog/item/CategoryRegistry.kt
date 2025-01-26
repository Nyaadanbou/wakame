package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File


@Init(
    stage = InitStage.POST_WORLD
)
@Reload
object CategoryRegistry : KoinComponent {
    private const val CATEGORY_DIR_NAME = "catalog/item/category"
    private val categories: MutableMap<String, Category> = mutableMapOf()
    private val logger: Logger by inject()

    /**
     * 获取所有物品类别
     */
    fun getCategoryMap(): Map<String, Category> {
        return categories.toMap()
    }

    operator fun get(id: String): Category? {
        return categories[id]
    }

    @InitFun
    private fun init() = loadDataIntoRegistry()

    @ReloadFun
    private fun reload() = loadDataIntoRegistry()


    fun loadDataIntoRegistry() {
        categories.clear()

        val categoryDir = get<File>(named(PLUGIN_DATA_DIR)).resolve(CATEGORY_DIR_NAME)
        categoryDir.walk()
            .drop(1)
            .filter { it.extension == "yml" }
            .forEach { file ->
                try {
                    val fileText = file.readText()
                    val id = file.nameWithoutExtension
                    val categoryNode = buildYamlConfigLoader {
                        withDefaults()
                        serializers {
                            kregister(CategorySerializer)
                        }
                    }.buildAndLoadString(fileText)
                    categoryNode.hint(CategorySerializer.HINT_NODE, id)
                    val category = categoryNode.krequire<Category>()
                    categories[id] = category

                } catch (e: Throwable) {
                    Util.pauseInIde(IllegalStateException("Can't register item catalog category: '${file.relativeTo(categoryDir)}'", e))
                }
            }

        logger.info("Registered guidebook item categories: {}", categories.keys.joinToString())
    }
}