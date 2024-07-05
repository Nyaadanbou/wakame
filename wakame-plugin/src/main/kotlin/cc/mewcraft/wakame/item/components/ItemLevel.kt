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
import cc.mewcraft.wakame.item.components.ItemLevel.Template.Option
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.toStableShort
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

data class ItemLevel(
    /**
     * 物品的等级.
     */
    val level: Short,
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<ItemLevel>, ItemComponentConfig(ItemComponentConstants.LEVEL) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { LEVEL }
        private val tooltipText: SingleTooltip = SingleTooltip()

        override fun codec(id: String): ItemComponentType<ItemLevel> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<ItemLevel> {
            return TemplateType
        }
    }

    override fun provideTooltipLore(): LoreLine {
        if (!showInTooltip) {
            return LoreLine.noop()
        }
        return LoreLine.simple(tooltipKey, listOf(tooltipText.render(Placeholder.component("level", Component.text(level.toInt())))))
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemLevel> {
        override fun read(holder: ItemComponentHolder): ItemLevel? {
            val tag = holder.getTag() ?: return null
            val raw = tag.getShort(TAG_VALUE)
            return ItemLevel(level = raw)
        }

        override fun write(holder: ItemComponentHolder, value: ItemLevel) {
            val tag = holder.getTagOrCreate()
            val raw = value.level
            tag.putShort(TAG_VALUE, raw)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
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
    private data class Template(
        val level: Any,
    ) : ItemTemplate<ItemLevel> {
        override val componentType: ItemComponentType<ItemLevel> = ItemComponentTypes.LEVEL

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
                .let { GenerationResult.of(ItemLevel(level = it)) }
        }

        enum class Option {
            CONTEXT
        }
    }

    private data object TemplateType : ItemTemplateType<ItemLevel> {
        override val typeToken: TypeToken<ItemTemplate<ItemLevel>> = typeTokenOf()

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
        override fun deserialize(type: Type, node: ConfigurationNode): ItemTemplate<ItemLevel> {
            return when (val scalar = node.rawScalar()) {
                is Number -> Template(scalar)
                is String -> Template(EnumLookup.lookup<Option>(scalar).getOrThrow())
                else -> throw SerializationException(node, type, "Invalid value type")
            }
        }
    }
}
