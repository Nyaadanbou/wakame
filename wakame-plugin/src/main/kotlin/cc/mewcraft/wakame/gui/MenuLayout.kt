package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.display2.NekoItemHolder
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.plain
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationVisitor
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 菜单的界面布局
 * 涉及Gui图标和语言文件
 */
internal data class MenuLayout(
    /**
     * 菜单的标题
     */
    val title: Component,
    /**
     * 菜单的结构
     */
    val structure: Array<String>,
    /**
     * 菜单中的图标
     */
    private val icons: Map<String, Key>,
    /**
     * 菜单中的各种语言文件
     */
    private val lang: Map<String, String>,
) : KoinComponent {

    private val logger = get<Logger>()

    /**
     * 通过配置文件中的节点路径, 获取对应图标的萌芽物品标识.
     *
     * ```yaml
     * icons:
     *   id_1: "menu:icon_1"
     *   id_2: "menu:icon_2"
     * ```
     */
    fun getIcon(id: String): NekoStack {
        val key = icons[id] ?: run {
            logger.warn("Menu icon '$id' not found in layout '${title.plain}', using default icon")
            return ItemRegistry.ERROR_NEKO_STACK
        }
        return NekoItemHolder.get(key).createNekoStack()
    }

    /**
     * 通过配置文件中的节点路径, 获取对应的文字内容.
     *
     * 节点路径的格式举例:
     *
     * - `"a"` -> `lang_1`
     * - `"b.a"` -> `lang_2`
     * - `"b.b.a"` -> `lang_3`
     * - `"b.b.b"` -> `lang_4`
     *
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
    fun getLang(key: String): String? {
        return lang[key]
    }

    // IDEA auto generated code

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is MenuLayout)
            return false

        if (title != other.title)
            return false
        if (!structure.contentEquals(other.structure))
            return false
        if (icons != other.icons)
            return false
        if (lang != other.lang)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + structure.contentHashCode()
        result = 31 * result + icons.hashCode()
        result = 31 * result + lang.hashCode()
        return result
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
        // TODO 使用真正的i18n
        val lang: MutableMap<String, String> = mutableMapOf()
        node.node("lang").visit(ConfigurationVisitor.Stateless { node1 ->
            if (node1.rawScalar() != null) {
                val langKeyArray = node1.path().array()
                val sliceArray = langKeyArray.sliceArray(langKeyArray.indexOf("lang") + 1 until langKeyArray.size)
                lang[sliceArray.joinToString(separator = ".")] = node1.rawScalar().toString()
            }
        })
        return MenuLayout(
            title = title.mini,
            structure = structure.toTypedArray(),
            icons = icons,
            lang = lang
        )
    }
}