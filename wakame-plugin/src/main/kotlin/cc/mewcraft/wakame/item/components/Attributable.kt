package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemConstants
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
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode

@Deprecated("与 ItemSlot 有高度重合")
interface Attributable : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<Attributable>, ItemComponentMeta {
        /**
         * 返回 [Attributable] 的实例.
         */
        fun of(): Attributable {
            return Value
        }

        override fun codec(id: String): ItemComponentType<Attributable> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.ATTRIBUTABLE
        override val tooltipKey: TooltipKey = ItemConstants.createKey { ATTRIBUTABLE }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.SingleTooltip = config.SingleTooltip()
    }

    // 开发日记: 2024/6/24
    // Attributable 本身没有“数值”一说, 只有是否存在于物品上一说.
    // 因此这里的 Value 类就写成单例了 (反正也没有任何数值).
    // 但需要注意的是, 即便是单例也依然提供了 LoreLine 的具体实现,
    // 这是因为我们 *有可能* 希望那些拥有 Attributable 组件的物品的提示框里
    // 能够显示一行 “提供属性加成” 的文本.
    private data object Value : Attributable {
        override fun provideTooltipLore(): LoreLine {
            if (!config.showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltip.render()))
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Attributable> {
        override fun read(holder: ItemComponentHolder): Attributable? {
            return if (holder.hasTag()) Value else null
        }

        override fun write(holder: ItemComponentHolder, value: Attributable) {
            holder.editTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    // 开发日记 2024/7/2
    // 如果我们需要给物品加上一个标记,
    // 但这个标记不储存在物品(NBT)上,
    // 而是存在模板里. 是否可行?
    data object Template : ItemTemplate<Attributable> {
        override val componentType: ItemComponentType<Attributable> = ItemComponentTypes.ATTRIBUTABLE

        override fun generate(context: GenerationContext): GenerationResult<Attributable> {
            return GenerationResult.of(Value)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            return Template
        }
    }
}
