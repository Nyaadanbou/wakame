package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
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
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
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
    ) : ItemComponentType<ItemRarity> {
        override fun read(holder: ItemComponentHolder): ItemRarity? {
            val tag = holder.getTag() ?: return null
            val raw = tag.getByteOrNull(TAG_VALUE)?.let(RarityRegistry::findBy) ?: return null
            return Value(rarity = raw)
        }

        override fun write(holder: ItemComponentHolder, value: ItemRarity) {
            val tag = holder.getTagOrCreate()
            tag.putByte(TAG_VALUE, value.rarity.binaryId)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
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
            fun warnNullItemLevel() {
                ItemComponentInjections.logger.warn("Failed to generate ${ItemComponentTypes.RARITY} for item '${context.target}' because no ${ItemComponentTypes.LEVEL} was found in the generation context")
            }

            val rarity: Rarity?

            when {
                // use static rarity
                static != null -> {
                    rarity = static
                }

                // use dynamic rarity
                dynamic != null -> {
                    val level = context.level
                    if (level != null) {
                        rarity = dynamic.pick(level.toInt(), context.random)
                    } else {
                        rarity = null
                        warnNullItemLevel()
                    }
                }

                // fallback to the global rarity mappings
                else -> {
                    val level = context.level
                    if (level != null) {
                        rarity = LevelMappingRegistry.INSTANCES[LevelMappingRegistry.GLOBAL_NAME].pick(level.toInt(), context.random)
                    } else {
                        rarity = null
                        warnNullItemLevel()
                    }
                }
            }

            // leave trace to the context
            context.rarity = rarity

            if (rarity != null) {
                return GenerationResult.of(Value(rarity = rarity))
            }

            return GenerationResult.empty()
        }

        companion object : ItemTemplateType<Template> {
            private const val MAPPING_PREFIX = "mapping:"
            private const val RARITY_PREFIX = "rarity:"

            override val typeToken: TypeToken<Template> = typeTokenOf()

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
