package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.item2.data.impl.Core
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeFacadeRegistryLoader::class
    ]
)
@Reload
internal object LootTableRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.LOOT_TABLE.resetRegistry()
        consumeData(BuiltInRegistries.LOOT_TABLE::add)
        BuiltInRegistries.LOOT_TABLE.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(BuiltInRegistries.LOOT_TABLE::update)
    }

    private fun consumeData(registryAction: (Identifier, LootTable<*>) -> Unit) {
        val dataDir = getFileInConfigDirectory("loot_table/")

        val loader = yamlLoader {
            withDefaults()

            serializers {
                registerAll(Core.serializers())
            }
        }

        for ((file, _, path) in NamespacedFileTreeWalker(dataDir, fileExtension = "yml", includeFullPath = true, includeNamespacePath = true)) {
            val rootNode = loader.buildAndLoadString(file.readText())
            val lootTableId = Identifiers.of(path)
            try {
                registryAction(lootTableId, rootNode.require<LootTable<*>>())
            } catch (t: Throwable) {
                LOGGER.warn("Failed to load loot table: '$lootTableId', Path: '${file.path}'", t)
                continue
            }
        }
    }
}