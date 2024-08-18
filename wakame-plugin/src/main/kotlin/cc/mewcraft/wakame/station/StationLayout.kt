package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * [Station] 的界面布局
 * 涉及Gui图标和语言文件
 */
data class StationLayout(
    val title: Component,
    val structure: List<String>,
    val icons: Map<String, Key>,
    val sufficientPrefixLang: String,
    val insufficientPrefixLang: String,
    val choicesLang: Map<String, String>,
    val resultsLang: Map<String, String>,
    val recipeNameLang: String,
    val recipeLoreLang: List<String>
){
    val isSimple = structure.map { it.toCharArray() }
        .reduce { acc, chars -> acc + chars }
        .distinct()
        .none { StationLayoutSerializer.CATEGORY_CHARS.contains(it) }
}

/**
 * [StationLayout] 的序列化器.
 */
internal object StationLayoutSerializer : TypeSerializer<StationLayout>, KoinComponent {
    private const val BACKGROUND_CHAR = 'X'
    private const val RECIPE_CHAR = '.'
    private const val PREV_PAGE_CHAR = '<'
    private const val NEXT_PAGE_CHAR = '>'
    private const val CATEGORY_0_CHAR = '0'
    private const val CATEGORY_1_CHAR = '1'
    private const val CATEGORY_2_CHAR = '2'
    private const val CATEGORY_3_CHAR = '3'
    private const val CATEGORY_4_CHAR = '4'
    private const val CATEGORY_5_CHAR = '5'
    val LEGAL_CHARS = listOf(
        BACKGROUND_CHAR, RECIPE_CHAR, PREV_PAGE_CHAR, NEXT_PAGE_CHAR,
        CATEGORY_0_CHAR, CATEGORY_1_CHAR, CATEGORY_2_CHAR, CATEGORY_3_CHAR, CATEGORY_4_CHAR, CATEGORY_5_CHAR
    )
    val CATEGORY_CHARS = listOf(
        CATEGORY_0_CHAR, CATEGORY_1_CHAR, CATEGORY_2_CHAR, CATEGORY_3_CHAR, CATEGORY_4_CHAR, CATEGORY_5_CHAR
    )

    override fun deserialize(type: Type, node: ConfigurationNode): StationLayout {
        val title = node.node("title").getString("Untitled Station")
        val structure = node.node("structure").getList<String>(emptyList()).apply {
            require(isNotEmpty()) { "Menu structure is not present" }
            val illegalChars = this.map { it.toCharArray() }
                .reduce { acc, chars -> acc + chars }
                .distinct()
                .filter { !LEGAL_CHARS.contains(it) }
            if (illegalChars.isNotEmpty()) {
                throw SerializationException("The chars [${illegalChars.joinToString(prefix = "'", postfix = "'")}] are illegal in the menu structure")
            }
        }
        val icons: Map<String, Key> = node.node("icons")
            .childrenMap()
            .mapKeys { (nodeKey, _) ->
                nodeKey.toString()
            }
            .mapValues { (_, mapChild) ->
                mapChild.krequire<Key>()
            }
        val sufficientPrefixLang = node.node("lang", "prefix", "sufficient").getString("✔")
        val insufficientPrefixLang = node.node("lang", "prefix", "sufficient").getString("✖")
        val choicesLang: Map<String, String> = node.node("lang", "choices")
            .childrenMap()
            .mapKeys { (nodeKey, _) ->
                nodeKey.toString()
            }
            .mapValues { (_, mapChild) ->
                mapChild.krequire<String>()
            }
        val resultsLang: Map<String, String> = node.node("lang", "results")
            .childrenMap()
            .mapKeys { (nodeKey, _) ->
                nodeKey.toString()
            }
            .mapValues { (_, mapChild) ->
                mapChild.krequire<String>()
            }
        val recipeNameLang = node.node("lang", "name").getString("合成: <result>")
        val recipeLoreLang: List<String> = node.node("lang", "lore").getList<String>(
            listOf(
                "合成所需材料:",
                "<choices>"
            )
        )
        return StationLayout(
            title = title.mini,
            structure = structure,
            icons = icons,
            sufficientPrefixLang = sufficientPrefixLang,
            insufficientPrefixLang = insufficientPrefixLang,
            choicesLang = choicesLang,
            resultsLang = resultsLang,
            recipeNameLang = recipeNameLang,
            recipeLoreLang = recipeLoreLang
        )
    }
}