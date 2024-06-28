package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// TODO 完成组件: ItemName

interface ItemName : Examinable {

    val raw: String
    val cooked: Component

    /* data */ class Value(
        override val raw: String,
        override val cooked: Component,
    ) : ItemName

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemName, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemName {
            val raw = holder.tag.getString(TAG_VALUE)
            val cooked = Component.empty()
            return Value(raw, cooked)
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemName) {
            holder.tag.putString(TAG_VALUE, value.raw)
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    data class Template(
        /**
         * A MiniMessage string.
         */
        val itemName: String?,
    ) : ItemTemplate<ItemName> {
        override fun generate(context: GenerationContext): GenerationResult<ItemName> {
            if (itemName == null) {
                return GenerationResult.empty()
            }
            val raw = itemName
            val cooked = Component.empty()
            return GenerationResult.of(Value(raw, cooked))
        }

        companion object : ItemTemplateType<Template> {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            /**
             * ## Node structure
             * ```yaml
             * <node>: <string>
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return Template(node.string)
            }
        }
    }
}