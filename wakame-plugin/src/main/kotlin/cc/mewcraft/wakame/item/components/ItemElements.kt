package cc.mewcraft.wakame.item.components

import cc.mewcraft.commons.collections.mapToByteArray
import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.element.ELEMENT_ITEM_PROTO_SERIALIZERS
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.random2.Pool
import cc.mewcraft.wakame.random2.PoolSerializer
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.getByteArrayOrNull
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

interface ItemElements : Examinable, TooltipProvider {

    /**
     * 所有的元素.
     */
    val elements: Set<Element>

    data class Value(
        override val elements: Set<Element>,
    ) : ItemElements {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, tooltipText.render(elements, Element::displayName))
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.ELEMENTS) {
            val tooltipKey: TooltipKey = ItemComponentConstants.createKey { ELEMENTS }
            val tooltipText: MergedTooltip = MergedTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemElements> {

        override fun read(holder: ItemComponentHolder): ItemElements? {
            val elementSet = holder.getTag()
                ?.getByteArrayOrNull(TAG_VALUE)
                ?.mapTo(ObjectArraySet(2), ElementRegistry::getBy)
                ?: return null
            return Value(elements = elementSet)
        }

        override fun write(holder: ItemComponentHolder, value: ItemElements) {
            require(value.elements.isNotEmpty()) { "The set of elements must not be empty" }
            val byteArray = value.elements.mapToByteArray(Element::binaryId)
            holder.getTagOrCreate().putByteArray(TAG_VALUE, byteArray)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    data class Template(
        val selector: Pool<Element, GenerationContext>,
    ) : ItemTemplate<ItemElements> {
        override fun generate(context: GenerationContext): GenerationResult<ItemElements> {
            val selected = selector.pickBulk(context).takeUnlessEmpty() ?: return GenerationResult.empty()
            val elements = Value(ObjectArraySet(selected))
            return GenerationResult.of(elements)
        }

        companion object : ItemTemplateType<Template>, KoinComponent {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return Template(node.krequire<Pool<Element, GenerationContext>>())
            }

            override fun childSerializers(): TypeSerializerCollection {
                return TypeSerializerCollection.builder()
                    .registerAll(get(named(ELEMENT_ITEM_PROTO_SERIALIZERS)))
                    .kregister(ElementPoolSerializer)
                    .build()
            }
        }
    }
}

/**
 * ## Node structure
 * ```yaml
 * <node>:
 *   sample: 2
 *   filters: [ ]
 *   entries:
 *     - value: neutral
 *       weight: 2
 *     - value: water
 *       weight: 1
 *     - value: fire
 *       weight: 1
 *     - value: wind
 *       weight: 1
 * ```
 */
internal data object ElementPoolSerializer : PoolSerializer<Element, GenerationContext>() {
    override fun sampleFactory(node: ConfigurationNode): Element {
        return node.node("value").krequire<Element>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }

    override fun onPickSample(content: Element, context: GenerationContext) {
        context.elements += content
    }
}