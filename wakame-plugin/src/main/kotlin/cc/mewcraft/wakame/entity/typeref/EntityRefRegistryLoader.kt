package cc.mewcraft.wakame.entity.typeref

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList

@Init(InitStage.BOOTSTRAP)
internal object EntityRefRegistryLoader : RegistryLoader {
    private const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        BuiltInRegistries.ENTITY_REF.resetRegistry()
        consumeData(BuiltInRegistries.ENTITY_REF::add)
        BuiltInRegistries.ENTITY_REF.freeze()
    }

    fun reload() {
        consumeData(BuiltInRegistries.ENTITY_REF::update)
    }

    private fun consumeData(registryAction: (KoishKey, EntityRef) -> Unit) {
        val loader = yamlLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.node("entity_type_holders").childrenMap()) {
            val entry = parseEntry(nodeKey, node)
            registryAction(entry.first, entry.second)
        }
    }

    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<KoishKey, EntityRef> {
        val resourceLocation = KoishKeys.of(nodeKey.toString())
        val keySet = node.getList<Key>(emptyList()).toSet()
        return Pair(resourceLocation, EntityRefImpl(keySet))
    }
}