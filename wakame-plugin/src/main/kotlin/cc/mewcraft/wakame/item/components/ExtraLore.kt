package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.ListTag
import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.component.ItemComponentType
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
import java.lang.reflect.Type

interface ExtraLore : Examinable, TooltipProvider {

    /**
     * 物品的额外描述.
     */
    val lore: List<String>

    data class Value(
        override val lore: List<String>,
    ) : ExtraLore {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            val lines = lore.mapTo(ObjectArrayList(lore.size)) { ItemComponentInjections.mini.deserialize(tooltipText.line, Placeholder.parsed("line", it)) }
            val header = tooltipText.header.run { mapTo(ObjectArrayList(this.size), ItemComponentInjections.mini::deserialize) }
            val bottom = tooltipText.bottom.run { mapTo(ObjectArrayList(this.size), ItemComponentInjections.mini::deserialize) }
            lines.addAll(0, header)
            lines.addAll(bottom)
            return LoreLine.simple(tooltipKey, lines)
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.LORE) {
            private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { LORE }
            private val tooltipText: LoreTooltip = LoreTooltip()
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ExtraLore, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ExtraLore? {
            val stringList = holder.tag.getListOrNull(TAG_VALUE, TagType.STRING)?.map { (it as StringTag).value() } ?: return null
            return Value(stringList)
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ExtraLore) {
            val stringTagList = value.lore.map(StringTag::valueOf)
            val stringListTag = ListTag.create(stringTagList, TagType.STRING)
            holder.tag.put(TAG_VALUE, stringListTag)
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    // 开发日记 2024/6/27
    // 模板中的描述文本应该始终是 MiniMessage 吗?
    // 我们可以让用户输入 MiniMessage, 但最终储存在内存里的
    // 数据可以是 Component?
    data class Template(
        /**
         * A list of MiniMessage strings.
         */
        val lore: List<String>,
    ) : ItemTemplate<ExtraLore> {
        override fun generate(context: GenerationContext): GenerationResult<ExtraLore> {
            return GenerationResult.of(Value(lore))
        }

        companion object : ItemTemplateType<Template> {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return Template(node.getList<String>(emptyList()))
            }
        }
    }
}
