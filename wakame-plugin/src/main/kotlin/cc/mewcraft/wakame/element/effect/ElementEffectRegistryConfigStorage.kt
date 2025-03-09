package cc.mewcraft.wakame.element.effect

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementRegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.ImmutableMultimap

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
internal object ElementEffectRegistryConfigStorage : RegistryConfigStorage {
    private var systems: ImmutableMultimap<InternKey, ElementEffects> = ImmutableMultimap.of()

    @InitFun
    private fun init() {
        loadElementEffects()
    }

    @ReloadFun
    private fun reload() {
        loadElementEffects()
    }

    operator fun get(element: RegistryEntry<out Element>, count: Int): Set<ElementEffects> {
        return systems.get(InternKey(element, count)).toSet()
    }

    private fun loadElementEffects() {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(ElementRegistryConfigStorage.FILE_PATH).readText())
        val elementEffects = ImmutableMultimap.builder<InternKey, ElementEffects>()
        for ((nodeKey, node) in rootNode.node("element_effects").childrenMap()) {
            val element = KoishRegistries.ELEMENT.getEntryOrThrow(nodeKey.toString())
            for (effectNode in node.childrenList()) {
                val amount = effectNode.node("amount").krequire<Int>()
                val effects = effectNode.node("effects").krequire<List<String>>()
                    .mapNotNull { effect -> ElementEffects.entries.firstOrNull { it.name.equals(effect, ignoreCase = true) } }
                elementEffects.putAll(InternKey(element, amount), effects)
            }
        }

        systems = elementEffects.build()
    }

    private data class InternKey(val element: RegistryEntry<out Element>, val count: Int)
}