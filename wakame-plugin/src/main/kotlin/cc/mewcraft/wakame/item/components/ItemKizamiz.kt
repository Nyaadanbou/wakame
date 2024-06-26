package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.filter.FilterFactory
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.random2.Group
import cc.mewcraft.wakame.random2.GroupSerializer
import cc.mewcraft.wakame.random2.Pool
import cc.mewcraft.wakame.random2.PoolSerializer
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.util.getByteArrayOrNull
import cc.mewcraft.wakame.util.krequire
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface ItemKizamiz : Examinable, TooltipProvider {

    /**
     * 所有的铭刻.
     */
    val kizamiz: Set<Kizami>

    data class Value(
        override val kizamiz: Set<Kizami>,
    ) : ItemKizamiz {
        override fun provideDisplayLore(): LoreLine {
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

    class Codec(
        override val id: String,
    ) : ItemComponentType<ItemKizamiz, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemKizamiz? {
            val kizamiz: Set<Kizami> = holder.tag.getByteArrayOrNull(TAG_VALUE)?.mapTo(ObjectArraySet(4)) { KizamiRegistry.getBy(it) } ?: return null
            return Value(kizamiz)
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemKizamiz) {
            require(value.kizamiz.isNotEmpty()) { "The set of kizami must be not empty" }
            val raw: ByteArray = value.kizamiz.map { it.binaryId }.toByteArray()
            holder.tag.putByteArray(TAG_VALUE, raw)
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }

        private companion object {
            const val TAG_VALUE = "value"
        }
    }

    data class Template(
        val selector: Group<Kizami, GenerationContext>,
    ) : ItemTemplate<ItemKizamiz> {
        override fun generate(context: GenerationContext): GenerationResult<ItemKizamiz> {
            val selected = selector.pickBulk(context).toSet()
            return if (selected.isNotEmpty()) {
                GenerationResult.of(Value(selected))
            } else {
                GenerationResult.empty()
            }
        }

        companion object : ItemTemplateType<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return Template(node.krequire<Group<Kizami, GenerationContext>>())
            }
        }
    }
}

internal object KizamiGroupSerializer : GroupSerializer<Kizami, GenerationContext>() {
    override fun poolFactory(node: ConfigurationNode): Pool<Kizami, GenerationContext> {
        return node.krequire<Pool<Kizami, GenerationContext>>()
    }

    override fun filterFactory(node: ConfigurationNode): Filter<GenerationContext> {
        return FilterFactory.create(node)
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
        return FilterFactory.create(node)
    }

    override fun onPickSample(content: Kizami, context: GenerationContext) {
        context.kizamiz += content
    }
}