package cc.mewcraft.wakame.world.entity

import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.core.ResourceLocation
import cc.mewcraft.wakame.core.ResourceLocations
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
object EntityTypeHolderRegistryConfigStorage : RegistryConfigStorage {
    const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        KoishRegistries.ENTITY_TYPE_HOLDER.resetRegistry()
        applyDataToRegistry(KoishRegistries.ENTITY_TYPE_HOLDER::register)
        KoishRegistries.ENTITY_TYPE_HOLDER.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.ENTITY_TYPE_HOLDER::update)
    }

    private fun applyDataToRegistry(registryAction: (ResourceLocation, EntityTypeHolder) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.node("entity_type_holders").childrenMap()) {
            val (resourceLocation, entityTypeHolder) = parseEntry(nodeKey, node)
            registryAction.invoke(resourceLocation, entityTypeHolder)
        }
    }

    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<ResourceLocation, EntityTypeHolder> {
        val resourceLocation = ResourceLocations.withKoishNamespace(nodeKey.toString())
        val keySet = node.getList<Key>(emptyList()).toSet()
        return Pair(resourceLocation, EntityTypeHolderImpl(keySet))
    }
}