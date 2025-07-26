package cc.mewcraft.koish.feature.townhall.techtree

import cc.mewcraft.koish.feature.townhall.TownHallFeature
import cc.mewcraft.koish.feature.townhall.TownyRegistries
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader

@Init(stage = InitStage.PRE_WORLD)
@Reload
object TechTreeRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        TownyRegistries.TECH_TREES.resetRegistry()
        consumeData(TownyRegistries.TECH_TREES::add)
        TownyRegistries.TECH_TREES.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(TownyRegistries.TECH_TREES::update)
    }

    private fun consumeData(registryAction: (Identifier, TechTree) -> Unit) {
        val dataDir = getFileInFeatureDirectory(TownHallFeature.namespace, "tech_trees/")

        val loader = yamlLoader {
            withDefaults()

            serializers {
                register(TechTree.SERIALIZER)
            }
        }

        for ((file, namespace, path) in NamespacedFileTreeWalker(dataDir, fileExtension = "yml")) {
            val rootNode = loader.buildAndLoadString(file.readText())
            val treeId = Identifiers.of(path)
            try {
                registryAction(treeId, rootNode.require())
            } catch (t: Throwable) {
                LOGGER.warn("Failed to load ability: '$treeId', Path: '${file.path}'", t)
                continue
            }
        }
    }
}