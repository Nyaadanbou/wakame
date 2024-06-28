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
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

interface ItemLevel : Examinable, TooltipProvider {

    /**
     * 物品的等级.
     */
    val level: Short

    data class Value(
        override val level: Short,
    ) : ItemLevel {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render(Placeholder.component("level", Component.text(level.toInt())))))
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.LEVEL) {
            private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { LEVEL }
            private val tooltipText: SingleTooltip = SingleTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemLevel, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemLevel {
            val raw = holder.tag.getShort(TAG_VALUE)
            return Value(raw)
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemLevel) {
            val raw = value.level
            holder.tag.putShort(TAG_VALUE, raw)
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }

        private companion object {
            const val TAG_VALUE = "value"
        }
    }

    /**
     * 物品的等级(模板).
     *
     * 等级的生成目前有两种模式: 固定等级, 动态等级.
     *
     * # 固定等级
     * 直接在配置文件中指定好一个常数, 然后每次都按照该常数生成等级.
     *
     * # 动态等级
     * 由生成的上下文决定要生成的等级.
     */
    data class Template(
        val level: Any,
    ) : ItemTemplate<ItemLevel> {
        override fun generate(context: GenerationContext): GenerationResult<ItemLevel> {
            val raw: Short = when (level) {
                is Number -> {
                    level.toStableShort()
                }

                is Option -> {
                    when (level) {
                        Option.CONTEXT -> context.trigger.level.toStableShort()
                    }
                }

                else -> {
                    throw IllegalStateException("Something wrong with ${this::class.simpleName}")
                }
            }

            return raw
                .coerceAtLeast(0) // by design, level never goes down below 0
                .also { context.level = it } // populate the context with generated level
                .let { GenerationResult.of(Value(it)) }
        }

        enum class Option {
            CONTEXT
        }

        companion object : ItemTemplateType<Template> {
            /**
             * ## Node structure 1
             * ```yaml
             * <node>: <int>
             * ```
             *
             * ## Node structure 2
             * ```yaml
             * <node>: <enum>
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return when (val scalar = node.rawScalar()) {
                    is Number -> Template(scalar)
                    is String -> Template(EnumLookup.lookup<Option>(scalar).getOrThrow())
                    else -> throw SerializationException(node, type, "Invalid value type")
                }
            }
        }
    }
}
