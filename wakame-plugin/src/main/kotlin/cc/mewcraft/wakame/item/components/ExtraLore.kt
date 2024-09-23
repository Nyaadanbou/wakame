package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.ListTag
import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.getListOrNull
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList

data class ExtraLore(
    /**
     * 物品的额外描述.
     */
    val lore: List<String>,
) : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<ExtraLore>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<ExtraLore> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.LORE
        override val tooltipKey: TooltipKey = ItemConstants.createKey { LORE }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.LoreTooltip = config.LoreTooltip()
    }

    override fun provideTooltipLore(): LoreLine {
        if (!config.showInTooltip) {
            return LoreLine.noop()
        }
        val lines = lore.mapTo(ObjectArrayList(lore.size)) { ItemComponentInjections.miniMessage.deserialize(tooltip.line, Placeholder.parsed("line", it)) }
        val header = tooltip.header.run { mapTo(ObjectArrayList(this.size), ItemComponentInjections.miniMessage::deserialize) }
        val bottom = tooltip.bottom.run { mapTo(ObjectArrayList(this.size), ItemComponentInjections.miniMessage::deserialize) }
        lines.addAll(0, header)
        lines.addAll(bottom)
        return LoreLine.simple(tooltipKey, lines)
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ExtraLore> {
        override fun read(holder: ItemComponentHolder): ExtraLore? {
            val tag = holder.getTag() ?: return null
            val stringList = tag.getListOrNull(TAG_VALUE, TagType.STRING)?.map { (it as StringTag).value() } ?: return null
            return ExtraLore(lore = stringList)
        }

        override fun write(holder: ItemComponentHolder, value: ExtraLore) {
            holder.editTag { tag ->
                val stringTagList = value.lore.map(StringTag::valueOf)
                val stringListTag = ListTag.create(stringTagList, TagType.STRING)
                tag.put(TAG_VALUE, stringListTag)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    // 开发日记 2024/6/27
    // 模板中的描述文本应该始终是 MiniMessage 吗?
    // 我们可以让用户输入 MiniMessage, 但最终储存在内存里的
    // 数据可以是 Component?
    //
    // 开发日记 2024/7/10
    // 还是得使用 MiniMessage. 因为目前的系统在设计上
    // 支持在模板 lore 的基础之上添加额外的内容. 这一
    // 机制依赖于 MiniMessage 的解析.
    data class Template(
        val lore: List<String>,
    ) : ItemTemplate<ExtraLore> {
        override val componentType: ItemComponentType<ExtraLore> = ItemComponentTypes.LORE

        override fun generate(context: GenerationContext): GenerationResult<ExtraLore> {
            return GenerationResult.of(ExtraLore(lore))
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   - "MiniMessage string"
         *   - "MiniMessage string"
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            return Template(node.getList<String>(emptyList()))
        }
    }
}
