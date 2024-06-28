package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// TODO 完成组件: ItemCells

interface ItemCells : Examinable, TooltipProvider {

    // TODO 2024/6/25
    //  ItemCells 需要返回多个 LoreLine
    /* data */ class Value : ItemCells {
        override fun provideDisplayLore(): LoreLine {
            return LoreLine.noop()
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.CELLS) {

        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemCells, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemCells {
            return Value()
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemCells) {

        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }

        private companion object {
            // 把原本的那些序列化用到的常量移到这里
        }
    }

    /* data */ class Template : ItemTemplate<ItemCells> {
        override fun generate(context: GenerationContext): GenerationResult<ItemCells> {
            return GenerationResult.empty()
        }

        companion object : ItemTemplateType<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return Template()
            }
        }
    }
}