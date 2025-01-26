package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload()
object LevelRarityMappingRegistryConfigStorage : RegistryConfigStorage {
    const val FILE_PATH = "levels.yml"

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
                kregister(LevelRarityMappingSerializer)
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
        val mapping = node.krequire<LevelRarityMapping>()
        return id to mapping
    }
}