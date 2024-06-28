package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.components.Arrow.Template
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface Attributable : Examinable, TooltipProvider {

    // 开发日记: 2024/6/24
    // Attributable 本身没有“数值”一说, 只有是否存在于物品上一说.
    // 因此这里 Value 写成单例就了 (反正也没有任何数值).
    // 但需要注意的是, 即便是单例也依然提供了 LoreLine 的具体实现,
    // 这是因为我们 !有可能! 希望那些拥有 Attributable 组件的物品的提示框里
    // 能够显示一行 “提供属性加成” 的文本, 当然, 也可以选择不写这个实现.
    companion object Value : Attributable, ItemComponentConfig(ItemComponentConstants.ATTRIBUTABLE) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { ATTRIBUTABLE }
        private val tooltipText: SingleTooltip = SingleTooltip()

        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Attributable, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        // 开发日记: 2024/6/26
        // NonValued + Holder.NBT 类型的 Codec 需要返回该组件的单例
        override fun read(holder: ItemComponentHolder.NBT): Attributable = Value

        // 开发日记: 2024/6/25
        // 这个执行什么无所谓, 因为实际运行逻辑是在 Map 里
        override fun write(holder: ItemComponentHolder.NBT, value: Attributable) = Unit

        // 开发日记: 2024/6/25
        // 这个执行什么无所谓, 因为实际运行逻辑是在 Map 里
        override fun remove(holder: ItemComponentHolder.NBT) = Unit
    }

    // 开发日记: 2024/6/24
    // Attributable 既然是一个 NonValued 组件类型,
    // 那么似乎也不需要为其创建一个 Template 的类型.
    // 在构建 NekoItem 的阶段只需要看配置文件里有没有这个 node 存在就行了?
    // 开发日记: 2024/6/25
    // 还不确定 Template 的具体框架是怎么样的
    data object Template : ItemTemplate<Attributable>, ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        override fun generate(context: GenerationContext): GenerationResult<Attributable> {
            return GenerationResult.of(Value)
        }

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return this
        }
    }
}
