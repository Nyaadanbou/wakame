package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.*
import java.io.File


@Init(stage = InitStage.POST_WORLD)
@Reload
object CategoryRegistryDataLoader : RegistryConfigStorage {
    const val DIR_PATH: String = "catalog/item/category/"

    @InitFun
    fun init() {
        KoishRegistries.ITEM_CATEGORY.resetRegistry()
        applyDataToRegistry(KoishRegistries.ITEM_CATEGORY::add)
        KoishRegistries.ITEM_CATEGORY.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.ITEM_CATEGORY::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, Category) -> Unit) {
        val dir = getFileInConfigDirectory(DIR_PATH)
        for (file in dir.walk().drop(1).filter(File::isFile)) {
            try {
                val id = Identifiers.of(file.nameWithoutExtension)
                val rootNode = buildYamlConfigLoader {
                    withDefaults()
                    serializers {
                        register<Category>(CategorySerializer)
                    }
                }.buildAndLoadString(file.readText())
                rootNode.hint(CategorySerializer.HINT_NODE, id)
                val category = rootNode.require<Category>()
                registryAction(id, category)
                LOGGER.info("Registered item catalog category: $id")
            } catch (e: Throwable) {
                LOGGER.error("Failed to load item catalog category from file: ${file.relativeTo(dir)}", e)
            }
        }
    }
}