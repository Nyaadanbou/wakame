package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode


// 开发日记 2024/7/2
// 如果我们需要给物品加上一个标记,
// 但这个标记不储存在物品(NBT)上,
// 而是存在模板里. 是否可行?
data object Attributable : ItemTemplate<Nothing>, ItemTemplateBridge<Attributable> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    override fun codec(id: String): ItemTemplateType<Attributable> {
        return TemplateType(id)
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Attributable> {
        override val type: TypeToken<Attributable> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun decode(node: ConfigurationNode): Attributable {
            return Attributable
        }
    }
}