package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplate
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface ItemCells : Examinable, TooltipProvider {

    // TODO 2024/6/25
    //  ItemCells 需要返回多个 LoreLine
    /* data */ class Value : ItemCells {
        companion object : ItemComponentConfig(ItemComponentConstants.CELLS) {

        }
    }

    class Codec(
        override val id: String,
    ) : ItemComponentType<ItemCells, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemCells? {
            TODO("Not yet implemented")
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemCells) {
            TODO("Not yet implemented")
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            TODO("Not yet implemented")
        }

        companion object {
            // 把原本的那些序列化用到的常量移到这里
        }
    }

    /* data */ class Template : ItemTemplate<ItemCells> {
        override fun generate(context: GenerationContext): GenerationResult<ItemCells> {
            TODO("Not yet implemented")
        }

        companion object : ItemTemplate.Serializer<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}