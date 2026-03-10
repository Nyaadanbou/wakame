package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.configurate.yamlLoader
import kotlin.io.path.*


@Init(InitStage.POST_WORLD)
internal object CatalogItemCategoryInitializer : RegistryLoader {

    @InitFun
    fun init() {
        DynamicRegistries.CATALOG_ITEM_CATEGORY.resetRegistry()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_CATEGORY::add)
        DynamicRegistries.CATALOG_ITEM_CATEGORY.freeze()
    }

    fun reload() {
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_CATEGORY::update)
    }

    private fun applyDataToRegistry(registryAction: (KoishKey, CatalogItemCategory) -> Unit) {
        val dir = KoishDataPaths.CONFIGS.resolve("catalog/item/category/")
        for (file in dir.walk().filter { it.extension == "yml" }) {
            try {
                val id = KoishKeys.of(file.nameWithoutExtension)
                val loader = yamlLoader {
                    withDefaults()
                    serializers {
                        register<CatalogItemCategory>(CatalogItemCategory.serializer())
                    }
                }
                val rootNode = loader.buildAndLoadString(file.readText())
                rootNode.hint(RepresentationHints.CATAGORY_ID, id)
                val category = rootNode.require<CatalogItemCategory>()
                registryAction(id, category)
                LOGGER.info("Registered item catalog category: $id")
            } catch (e: Throwable) {
                LOGGER.error("Failed to register item catalog category from file: ${file.relativeTo(dir)}", e)
            }
        }
    }
}