package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.RENDERER_CONFIG_LOADER
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

internal interface RendererConfiguration : Reloadable {
    val nameStyle: String
    val lorePrimaryOrders: Map<String, Int>

    /**
     * 会固定出现在 lore 中的文本内容。
     */
    val fixedLoreLines: List<FixedLoreLine>
}

internal class RendererConfigImpl : KoinComponent, RendererConfiguration {

    private lateinit var root: NekoConfigurationNode
    private val _fixedLoreLines = mutableListOf<FixedLoreLine>()

    override val nameStyle: String by reloadable { root.node("name_renderer_style").requireKt() }
    override val lorePrimaryOrders: Map<String, Int> by reloadable {
        val loreOrderNode = root.node("lore_renderer_order")

        val primaryList = loreOrderNode.node("primary").requireKt<List<String>>()
        val elementList = loreOrderNode.node("element").requireKt<List<String>>()
        val operationList = loreOrderNode.node("operation").requireKt<List<String>>()

        val primaryOrders = mutableMapOf<String, Int>()

        for ((index, key) in primaryList.withIndex()) {
            if (key.startsWith("^")) {
                // 根据约定, 固定内容的 key 以就是这一行内容本身。
                primaryOrders[key.substring(1)] = index
                // 加入到固定内容的 key 列表中。
                _fixedLoreLines.add(
                    FixedLoreLineImpl(
                        key = key.substring(1),
                        line = listOf(key.substring(1).mini)
                    )
                )
                continue
            }

            if (!key.startsWith("attribute:"))
                continue

            /* Generate all like attribute:attack_damage:operation:element */
            val attribute = key.substring(10)
            val meta = AttributeRegistry.getMeta(Key.key(NekoNamespaces.ATTRIBUTE, key))

            for (operation in operationList) {
                if (!meta.element) {
                    val newKey = "$attribute:$operation"
                    primaryOrders[newKey] = index
                    continue
                }

                for (element in elementList) {
                    val newKey = "$attribute:$operation:$element"
                    primaryOrders[newKey] = index
                }
            }

        }
        primaryOrders
    }

    override val fixedLoreLines: List<FixedLoreLine>
        get() = _fixedLoreLines

    private fun loadConfiguration() {
        root = get<NekoConfigurationLoader>(named(RENDERER_CONFIG_LOADER)).load()
        _fixedLoreLines.clear()
    }

    override fun onReload() {
        loadConfiguration()
    }
}