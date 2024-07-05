package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

data class ItemCrate(
    /**
     * 盲盒的唯一标识.
     */
    val key: Key,
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<ItemCrate>, ItemComponentConfig(ItemComponentConstants.CRATE) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { CRATE }
        private val tooltipText: SingleTooltip = SingleTooltip()

        override fun codec(id: String): ItemComponentType<ItemCrate> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<ItemCrate> {
            return TemplateType
        }
    }

    override fun provideTooltipLore(): LoreLine {
        if (!showInTooltip) {
            return LoreLine.noop()
        }
        return LoreLine.simple(tooltipKey, listOf(tooltipText.render(Placeholder.component("key", Component.text(key.asString())))))
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemCrate> {
        override fun read(holder: ItemComponentHolder): ItemCrate? {
            val tag = holder.getTag() ?: return null
            val key = Key(tag.getString(TAG_KEY))
            return ItemCrate(key = key)
        }

        override fun write(holder: ItemComponentHolder, value: ItemCrate) {
            holder.getTagOrCreate().putString(TAG_KEY, value.key.asString())
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_KEY = "key"
        }
    }

    private data class Template(
        /**
         * 盲盒的唯一标识.
         */
        val key: Key,
    ) : ItemTemplate<ItemCrate> {
        override val componentType: ItemComponentType<ItemCrate> = ItemComponentTypes.CRATE

        override fun generate(context: GenerationContext): GenerationResult<ItemCrate> {
            return GenerationResult.of(ItemCrate(key = key))
        }
    }

    private data object TemplateType : ItemTemplateType<ItemCrate> {
        override val typeToken: TypeToken<ItemTemplate<ItemCrate>> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   key: "foo:bar"
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): ItemTemplate<ItemCrate> {
            val raw = node.node("key").krequire<Key>()
            return Template(raw)
        }
    }
}