package cc.mewcraft.wakame.item.components

import cc.mewcraft.commons.collections.mapToByteArray
import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.kizami.KIZAMI_EXTERNALS
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.random2.Group
import cc.mewcraft.wakame.random2.GroupSerializer
import cc.mewcraft.wakame.random2.Pool
import cc.mewcraft.wakame.random2.PoolSerializer
import cc.mewcraft.wakame.registry.KizamiRegistry
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

interface ItemKizamiz : Examinable, TooltipProvider.Single {

    /**
     * 所有的铭刻.
     */
    val kizamiz: Set<Kizami>

    data class Value(
        override val kizamiz: Set<Kizami>,
    ) : ItemKizamiz {
        override fun provideTooltipLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, tooltipText.render(kizamiz, Kizami::displayName))
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.KIZAMIZ) {
            val tooltipKey: TooltipKey = ItemComponentConstants.createKey { KIZAMIZ }
            val tooltipText: MergedTooltip = MergedTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemKizamiz> {
        override fun read(holder: ItemComponentHolder): ItemKizamiz? {
            val tag = holder.getTag() ?: return null
            val kizamiSet = tag.getByteArrayOrNull(TAG_VALUE)?.mapTo(ObjectArraySet(4), KizamiRegistry::getBy) ?: return null
            return Value(kizamiz = kizamiSet)
        }

        override fun write(holder: ItemComponentHolder, value: ItemKizamiz) {
            require(value.kizamiz.isNotEmpty()) { "The set of kizami must be not empty" }
            val tag = holder.getTagOrCreate()
            val byteArray = value.kizamiz.mapToByteArray(Kizami::binaryId)
            tag.putByteArray(TAG_VALUE, byteArray)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    data class Template(
        val selector: Group<Kizami, GenerationContext>,
    ) : ItemTemplate<ItemKizamiz> {
        override val componentType: ItemComponentType<ItemKizamiz> = ItemComponentTypes.KIZAMIZ

        override fun generate(context: GenerationContext): GenerationResult<ItemKizamiz> {
            val selected = selector.pickBulk(context).takeUnlessEmpty() ?: return GenerationResult.empty()
            val kizamiz = Value(ObjectArraySet(selected))
            return GenerationResult.of(kizamiz)
        }

        companion object : ItemTemplateType<Template>, KoinComponent {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return Template(node.krequire<Group<Kizami, GenerationContext>>())
            }

            override fun childSerializers(): TypeSerializerCollection {
                return TypeSerializerCollection.builder()
                    .registerAll(get(named(KIZAMI_EXTERNALS)))
                    .kregister(KizamiPoolSerializer)
                    .kregister(KizamiGroupSerializer)
                    .build()
            }
        }
    }
}

internal object KizamiGroupSerializer : GroupSerializer<Kizami, GenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): Pool<Kizami, GenerationContext> {
        return node.krequire<Pool<Kizami, GenerationContext>>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }
}

/**
 * ## Configuration node structure
 *
 * ```yaml
 * <node>:
 *   sample: 2
 *   filters: [ ]
 *   entries:
 *     - value: wood
 *       weight: 3
 *     - value: iron
 *       weight: 2
 *     - value: gold
 *       weight: 1
 *     - value: diamond
 *       weight: 1
 *     - value: netherite
 *       weight: 1
 * ```
 */
internal object KizamiPoolSerializer : PoolSerializer<Kizami, GenerationContext>() {
    override fun sampleFactory(node: ConfigurationNode): Kizami {
        return node.node("value").krequire<Kizami>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire<Filter<GenerationContext>>()
    }

    override fun onPickSample(content: Kizami, context: GenerationContext) {
        context.kizamiz += content
    }
}