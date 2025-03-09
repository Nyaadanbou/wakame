package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.walk


@Init(stage = InitStage.POST_WORLD)
@Reload
internal object CatalogItemCategoryRegistryLoader : RegistryConfigStorage {

    @InitFun
    private fun init() {
        KoishRegistries.ITEM_CATEGORY.resetRegistry()
        applyDataToRegistry(KoishRegistries.ITEM_CATEGORY::add)
        KoishRegistries.ITEM_CATEGORY.freeze()
    }

    @ReloadFun
    private fun reload() {
        applyDataToRegistry(KoishRegistries.ITEM_CATEGORY::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, CatalogItemCategory) -> Unit) {
        val dir = KoishDataPaths.CONFIGS.resolve("catalog/item/category/")
        for (file in dir.walk().filter { it.extension == "yml" }) {
            try {
                val id = Identifiers.of(file.nameWithoutExtension)
                val loader = buildYamlConfigLoader {
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