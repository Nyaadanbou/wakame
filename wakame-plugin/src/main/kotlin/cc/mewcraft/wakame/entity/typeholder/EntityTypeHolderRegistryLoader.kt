package cc.mewcraft.wakame.entity.typeholder

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.world.entity.EntityTypeHolder
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
internal object EntityTypeHolderRegistryLoader : RegistryConfigStorage {
    const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        KoishRegistries.ENTITY_TYPE_HOLDER.resetRegistry()
        applyDataToRegistry(KoishRegistries.ENTITY_TYPE_HOLDER::add)
        KoishRegistries.ENTITY_TYPE_HOLDER.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.ENTITY_TYPE_HOLDER::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, EntityTypeHolder) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.node("entity_type_holders").childrenMap()) {
            val entry = parseEntry(nodeKey, node)
            registryAction(entry.first, entry.second)
        }
    }

    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, EntityTypeHolder> {
        val resourceLocation = Identifiers.of(nodeKey.toString())
        val keySet = node.getList<Key>(emptyList()).toSet()
        return Pair(resourceLocation, EntityTypeHolderImpl(keySet))
    }
}