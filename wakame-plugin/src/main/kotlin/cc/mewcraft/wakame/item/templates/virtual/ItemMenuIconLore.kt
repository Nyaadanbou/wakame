package cc.mewcraft.wakame.item.templates.virtual

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.MenuIconDictionary
import cc.mewcraft.wakame.util.MenuIconLore
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode

data class ItemMenuIconLore(
    val delegate: MenuIconLore,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    fun resolve(placeholderMap: MenuIconDictionary = MenuIconDictionary(), dsl: MenuIconLore.LineConfigBuilder.() -> Unit): List<Component> {
        return delegate.resolve(placeholderMap, dsl)
    }

    companion object : ItemTemplateBridge<ItemMenuIconLore> {
        override fun codec(id: String): ItemTemplateType<ItemMenuIconLore> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemMenuIconLore> {
        override val type: TypeToken<ItemMenuIconLore> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   - "line 1 (MiniMessage string)"
         *   - "line 2 (MiniMessage string)"
         * ```
         *
         * @see cc.mewcraft.wakame.util.MenuIconLore
         * @see cc.mewcraft.wakame.util.MenuIconLoreSerializer
         */
        override fun decode(node: ConfigurationNode): ItemMenuIconLore {
            return ItemMenuIconLore(node.krequire<MenuIconLore>())
        }
    }
}
