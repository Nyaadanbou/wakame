package cc.mewcraft.wakame.item.templates.virtual

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.MenuIcon
import cc.mewcraft.wakame.util.MenuIconDictionary
import cc.mewcraft.wakame.util.MenuIconName
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode

data class ItemMenuIconName(
    val delegate: MenuIconName,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    /**
     * @see MenuIconName.resolve
     */
    fun resolve(tagResolver: TagResolver): Component {
        return delegate.resolve(tagResolver)
    }

    /**
     * @see MenuIconName.resolve
     */
    fun resolve(dict: MenuIconDictionary = MenuIconDictionary(), dsl: MenuIcon.PlaceholderTagResolverBuilder.() -> Unit): Component {
        return delegate.resolve(dict, dsl)
    }

    companion object : ItemTemplateBridge<ItemMenuIconName> {
        override fun codec(id: String): ItemTemplateType<ItemMenuIconName> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemMenuIconName> {
        override val type: TypeToken<ItemMenuIconName> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: "item name (MiniMessage string)"
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemMenuIconName {
            return ItemMenuIconName(node.krequire<MenuIconName>())
        }
    }
}
