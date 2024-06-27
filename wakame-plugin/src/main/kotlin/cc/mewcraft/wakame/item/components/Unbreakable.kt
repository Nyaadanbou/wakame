package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// 开发日记 2024/6/27
// 之所以写这个组件是因为想验证一下
// 在配置文件中以 1:1 的形式配置原版物品组件
// 是否可行.

interface Unbreakable : Examinable, TooltipProvider {

    val showInTooltip: Boolean

    data class Value(
        override val showInTooltip: Boolean,
    ) : Unbreakable {
        private companion object
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Unbreakable, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Unbreakable? {
            val itemMeta = holder.item.itemMeta ?: return null
            if (!itemMeta.isUnbreakable) {
                return null
            }
            return Value(!itemMeta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE))
        }

        override fun write(holder: ItemComponentHolder.Item, value: Unbreakable) {
            holder.item.editMeta {
                it.isUnbreakable = true
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            holder.item.editMeta { it.isUnbreakable = false }
        }

        private companion object
    }

    data class Template(
        val showInTooltip: Boolean,
    ) : ItemTemplate<Value> {
        override fun generate(context: GenerationContext): GenerationResult<Value> {
            return GenerationResult.of(Value(showInTooltip))
        }

        companion object : ItemTemplateType<Template> {
            /**
             * ## Node structure
             * ```yaml
             * <root>:
             *   show_in_tooltip: <boolean>
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
                return Template(showInTooltip)
            }
        }
    }
}