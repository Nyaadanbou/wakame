package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
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

interface Crate : Examinable, TooltipProvider {

    /**
     * 盲盒的唯一标识.
     */
    val key: Key

    data class Value(
        override val key: Key,
    ) : Crate {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render(Placeholder.component("key", Component.text(key.asString())))))
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.CRATE) {
            val tooltipKey: TooltipKey = ItemComponentConstants.createKey { CRATE }
            val tooltipText: SingleTooltip = SingleTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Crate> {
        override fun read(holder: ItemComponentHolder): Crate? {
            val tag = holder.getTag() ?: return null
            val key = Key(tag.getString(TAG_KEY))
            return Value(key = key)
        }

        override fun write(holder: ItemComponentHolder, value: Crate) {
            holder.getTagOrCreate().putString(TAG_KEY, value.key.asString())
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_KEY = "key"
        }
    }

    data class Template(
        /**
         * 盲盒的唯一标识.
         */
        val key: Key,
    ) : ItemTemplate<Crate> {
        override fun generate(context: GenerationContext): GenerationResult<Crate> {
            return GenerationResult.of(Value(key))
        }

        companion object : ItemTemplateType<Template> {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            /**
             * ## Node structure
             * ```yaml
             * <node>:
             *   key: "foo:bar"
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                val raw = node.node("key").krequire<Key>()
                return Template(raw)
            }
        }
    }
}