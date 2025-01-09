package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.core.Identifier
import cc.mewcraft.wakame.core.Identifiers
import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
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
        applyDataToRegistry(KoishRegistries.ELEMENT::add)
        KoishRegistries.ELEMENT.freeze()

        // register the provider
        ElementProvider.register(BuiltInElementProvider)
    }

    @ReloadFun
    fun reload() {
        // update existing registry
        applyDataToRegistry(KoishRegistries.ELEMENT::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, ElementType) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.node("elements").childrenMap()) {
            val (id, element) = parseEntry(nodeKey, node)
            registryAction.invoke(id, element)
        }
    }

    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, ElementType> {
        val stringId = nodeKey.toString()
        val resourceLocation = Identifiers.ofKoish(stringId)
        val integerId = node.node("binary_index").krequire<Int>()
        val displayName = node.node("display_name").get<Component>(Component.text(stringId.replaceFirstChar(Char::titlecase)))
        val displayStyles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())

        return resourceLocation to ElementType(resourceLocation, stringId, integerId, displayName, displayStyles)
    }
}

private object BuiltInElementProvider : ElementProvider {
    override fun get(stringId: String): Element? {
        return KoishRegistries.ELEMENT.get(stringId)
    }
}