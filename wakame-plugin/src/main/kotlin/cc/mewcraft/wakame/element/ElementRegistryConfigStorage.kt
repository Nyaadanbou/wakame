package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.core.ResourceLocation
import cc.mewcraft.wakame.core.ResourceLocations
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registries.KoishRegistries
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
object ElementRegistryConfigStorage : RegistryConfigStorage {
    const val FILE_PATH: String = "elements.yml"

    @InitFun
    fun init() {
        KoishRegistries.ELEMENT.resetRegistry()
        applyDataToRegistry(KoishRegistries.ELEMENT::register)
        KoishRegistries.ELEMENT.freeze()

        // register the provider
        ElementProvider.register(BuiltInElementProvider)
    }

    @ReloadFun
    fun reload() {
        // update existing registry
        applyDataToRegistry(KoishRegistries.ELEMENT::update)
    }

    internal fun applyDataToRegistry(registryAction: (ResourceLocation, ElementType) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.node("elements").childrenMap()) {
            val (id, element) = parseEntry(nodeKey, node)
            registryAction.invoke(id, element)
        }
    }

    internal fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<ResourceLocation, ElementType> {
        val stringId = nodeKey.toString()
        val resourceLocation = ResourceLocations.withKoishNamespace(stringId)
        val integerId = node.node("binary_index").krequire<Int>()
        val displayName = node.node("display_name").get<Component>(Component.text(stringId.replaceFirstChar(Char::titlecase)))
        val displayStyles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())

        return resourceLocation to ElementType(resourceLocation, stringId, integerId, displayName, displayStyles)
    }
}

private object BuiltInElementProvider : ElementProvider {
    override fun get(stringId: String): Element? {
        return KoishRegistries.ELEMENT.getValue(stringId)
    }
}