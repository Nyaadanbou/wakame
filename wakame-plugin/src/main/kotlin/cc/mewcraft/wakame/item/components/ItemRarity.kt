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
import cc.mewcraft.wakame.rarity.LevelMappings
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.LevelMappingRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.getByteOrNull
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

interface ItemRarity : Examinable, TooltipProvider {

    /**
     * 物品的稀有度.
     */
    val rarity: Rarity

    data class Value(
        override val rarity: Rarity,
    ) : ItemRarity {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render(Placeholder.component("value", rarity.displayName))))
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.RARITY) {
            private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { RARITY }
            private val tooltipText: SingleTooltip = SingleTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemRarity, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemRarity? {
            val raw = holder.tag.getByteOrNull(TAG_VALUE)?.let(RarityRegistry::findBy) ?: return null
            return Value(raw)
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemRarity) {
            holder.tag.putByte(TAG_VALUE, value.rarity.binaryId)
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }

        private companion object {
            const val TAG_VALUE = "value"
        }
    }

    data class Template(
        /**
         * The default rarity held in this schema.
         */
        private val static: Rarity? = null,
        /**
         * The mappings used to generate the rarity.
         */
        private val dynamic: LevelMappings? = null,
    ) : ItemTemplate<ItemRarity> {

        init {
            require((static != null) xor (dynamic != null)) { "(static != null) xor (dynamic != null)" }
        }

        override fun generate(context: GenerationContext): GenerationResult<ItemRarity> {
            val rarity: Rarity = when {
                // use static rarity
                static != null -> static
                // use dynamic rarity
                dynamic != null -> dynamic.pick(context.level.toInt(), context.random)
                // fallback to the global rarity mappings
                else -> LevelMappingRegistry.INSTANCES[LevelMappingRegistry.GLOBAL_NAME].pick(context.level.toInt(), context.random)
            }.also {
                // leave trace to the context
                context.rarities += it
            }
            return GenerationResult.of(Value(rarity))
        }

        companion object : ItemTemplateType<Template> {
            private const val MAPPING_PREFIX = "mapping:"
            private const val RARITY_PREFIX = "rarity:"

            /**
             * ## Node structure 1
             * ```yaml
             * <node>: "mapping:_"
             * ```
             *
             * ## Node structure 2
             * ```yaml
             * <node>: "rarity:_"
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                val string = node.krequire<String>()
                return when {
                    string.startsWith(MAPPING_PREFIX) -> Template(
                        dynamic = LevelMappingRegistry.INSTANCES[string.substringAfter(MAPPING_PREFIX)],
                    )

                    string.startsWith(RARITY_PREFIX) -> Template(
                        static = RarityRegistry.INSTANCES[string.substringAfter(RARITY_PREFIX)]
                    )

                    else -> throw SerializationException("Can't parse rarity value '$string'")
                }
            }
        }
    }
}
