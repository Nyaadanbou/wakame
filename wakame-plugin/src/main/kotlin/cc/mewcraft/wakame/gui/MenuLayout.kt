package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationVisitor
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 菜单的界面布局
 * 涉及Gui图标和语言文件
 */
data class MenuLayout(
    /**
     * 菜单的标题
     */
    val title: Component,
    /**
     * 菜单的结构
     */
    val structure: List<String>,
    /**
     * 菜单中的图标
     */
    private val icons: Map<String, Key>,
    /**
     * 菜单中的各种语言文件
     */
    private val lang: Map<String, String>,
) {

    /**
     * 通过配置文件中的id获取对应的图标Key
     * ```yaml
     * icons:
     *   id_1: "menu:icon_1"
     *   id_2: "menu:icon_2"
     * ```
     */
    fun getIcon(id: String): Key? {
        return icons[id]
    }

    /**
     * 通过配置文件中的路径Key获取对应的lang
     * 路径Key格式举例:
     * "a" -> lang_1
     * "b.a" -> lang_2
     * "b.b.a" -> lang_3
     * "b.b.b" -> lang_4
     * ```yaml
     * lang:
     *   a: lang_1
     *   b:
     *     a: lang_2
     *     b:
     *       a: lang_3
     *       b: lang_4
     * ```
     */
    fun getLang(langKey: String): String? {
        return lang[langKey]
    }
}

/**
 * [MenuLayout] 的序列化器.
 */
internal object MenuLayoutSerializer : TypeSerializer<MenuLayout>, KoinComponent {
    override fun deserialize(type: Type, node: ConfigurationNode): MenuLayout {
        val title = node.node("title").getString("Untitled Layout")
        val structure = node.node("structure").getList<String>(emptyList()).apply {
            require(isNotEmpty()) { "Menu structure is not present" }
        }
        val icons: Map<String, Key> = node.node("icons")
            .childrenMap()
            .mapKeys { (nodeKey, _) ->
                nodeKey.toString()
            }
            .mapValues { (_, mapChild) ->
                mapChild.krequire<Key>()
            }
        val lang: MutableMap<String, String> = mutableMapOf()
        node.node("lang").visit(ConfigurationVisitor.Stateless { node1 ->
            if (node1.rawScalar() != null) {
                lang[node1.path().joinToString(separator = ".")] = node1.rawScalar().toString()
            }
        })
        return MenuLayout(
            title = title.mini,
            structure = structure,
            icons = icons,
            lang = lang
        )
    }
}