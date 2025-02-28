package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.*
import org.spongepowered.configurate.ConfigurationNode

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
object LevelRarityMappingRegistryLoader : RegistryConfigStorage {
    private const val FILE_PATH = "levels.yml"

    @InitFun
    fun init() {
        KoishRegistries.LEVEL_RARITY_MAPPING.resetRegistry()
        applyDataToRegistry(KoishRegistries.LEVEL_RARITY_MAPPING::add)
        KoishRegistries.LEVEL_RARITY_MAPPING.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.LEVEL_RARITY_MAPPING::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, LevelRarityMapping) -> Unit) {
        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<LevelRarityMapping>(LevelRarityMappingSerializer)
            }
        }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.childrenMap()) {
            val entry = parseEntry(nodeKey, node)
            registryAction(entry.first, entry.second)
        }
    }

    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, LevelRarityMapping> {
        val id = Identifiers.of(nodeKey.toString())
        val mapping = node.require<LevelRarityMapping>()
        return id to mapping
    }
}