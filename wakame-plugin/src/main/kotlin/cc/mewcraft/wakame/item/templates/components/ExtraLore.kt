package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import cc.mewcraft.wakame.item.components.ExtraLore as ExtraLoreData


// 开发日记 2024/6/27
// 模板中的描述文本应该始终是 MiniMessage 吗?
// 我们可以让用户输入 MiniMessage, 但最终储存在内存里的
// 数据可以是 Component?
// 开发日记 2024/7/10
// 还是得使用 MiniMessage. 因为目前的系统在设计上
// 支持在模板 lore 的基础之上添加额外的内容. 这一
// 机制依赖于 MiniMessage 的解析.
data class ExtraLore(
    val lore: List<String>,
) : ItemTemplate<ExtraLoreData> {
    override val componentType: ItemComponentType<ExtraLoreData> = ItemComponentTypes.LORE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ExtraLoreData> {
        return ItemGenerationResult.of(ExtraLoreData(lore))
    }

    companion object : ItemTemplateBridge<ExtraLore> {
        override fun codec(id: String): ItemTemplateType<ExtraLore> {
            return TemplateType(id)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<ExtraLore> {
        override val type: TypeToken<ExtraLore> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   - "MiniMessage string"
         *   - "MiniMessage string"
         * ```
         */
        override fun decode(node: ConfigurationNode): ExtraLore {
            return ExtraLore(node.getList<String>(emptyList()))
        }
    }
}