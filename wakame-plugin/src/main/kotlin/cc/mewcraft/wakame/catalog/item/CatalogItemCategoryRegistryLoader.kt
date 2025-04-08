package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.DynamicRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.*
import kotlin.io.path.*


@Init(stage = InitStage.POST_WORLD)
@Reload
internal object CatalogItemCategoryRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        DynamicRegistries.ITEM_CATEGORY.resetRegistry()
        applyDataToRegistry(DynamicRegistries.ITEM_CATEGORY::add)
        DynamicRegistries.ITEM_CATEGORY.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(DynamicRegistries.ITEM_CATEGORY::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, CatalogItemCategory) -> Unit) {
        val dir = KoishDataPaths.CONFIGS.resolve("catalog/item/category/")
        for (file in dir.walk().filter { it.extension == "yml" }) {
            try {
                val id = Identifiers.of(file.nameWithoutExtension)
                val loader = yamlLoader {
                    withDefaults()
                    serializers {
                        register<CatalogItemCategory>(CategorySerializer)
                    }
                }
                val rootNode = loader.buildAndLoadString(file.readText())
                rootNode.hint(RepresentationHints.CATAGORY_ID, id)
                val category = rootNode.require<CatalogItemCategory>()
                registryAction(id, category)
                LOGGER.info("Registered item catalog category: $id")
            } catch (e: Throwable) {
                LOGGER.error("Failed to load item catalog category from file: ${file.relativeTo(dir)}", e)
            }
        }
    }
}