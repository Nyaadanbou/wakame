package cc.mewcraft.wakame.item.components

import cc.mewcraft.commons.collections.mapToByteArray
import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
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
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.util.stream.Stream

data class ItemElements(
    /**
     * 所有的元素.
     */
    val elements: Set<Element>,
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<ItemElements>, ItemComponentMeta {
        fun of(elements: Collection<Element>): ItemElements {
            return ItemElements(ObjectArraySet(elements))
        }

        fun of(vararg elements: Element): ItemElements {
            return of(elements.toList())
        }

        override fun codec(id: String): ItemComponentType<ItemElements> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.ELEMENTS
        override val tooltipKey: TooltipKey = ItemComponentConstants.createKey { ELEMENTS }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.MergedTooltip = config.MergedTooltip()
    }

    override fun provideTooltipLore(): LoreLine {
        if (!config.showInTooltip) {
            return LoreLine.noop()
        }
        return LoreLine.simple(tooltipKey, tooltip.render(elements, Element::displayName))
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("elements", elements),
    )

    override fun toString(): String {
        return toSimpleString()
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemElements> {

        override fun read(holder: ItemComponentHolder): ItemElements? {
            val elementSet = holder.getTag()
                ?.getByteArrayOrNull(TAG_VALUE)
                ?.mapTo(ObjectArraySet(2), ElementRegistry::getBy)
                ?: return null
            return ItemElements(elements = elementSet)
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
        override val componentType: ItemComponentType<ItemElements> = ItemComponentTypes.ELEMENTS

        override fun generate(context: GenerationContext): GenerationResult<ItemElements> {
            val selected = selector.pickBulk(context).takeUnlessEmpty() ?: return GenerationResult.empty()
            val elements = ItemElements(ObjectArraySet(selected))
            return GenerationResult.of(elements)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template>, KoinComponent {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <pool>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            return Template(node.krequire<Pool<Element, GenerationContext>>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .registerAll(get(named(ELEMENT_EXTERNALS)))
                .kregister(ElementPoolSerializer)
                .build()
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
private data object ElementPoolSerializer : PoolSerializer<Element, GenerationContext>() {
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