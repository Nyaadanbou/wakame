package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
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
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// 开发日记 2024/6/27
// 之所以写这个组件是因为想验证一下
// 在配置文件中以 1:1 的形式配置原版物品组件
// 是否可行.

interface Unbreakable : Examinable {

    val showInTooltip: Boolean

    companion object : ItemComponentBridge<Unbreakable>, ItemComponentMeta {
        override val configPath: String = ItemComponentConstants.UNBREAKABLE
        override val tooltipKey: Key = ItemComponentConstants.createKey { UNBREAKABLE }

        override fun codec(id: String): ItemComponentType<Unbreakable> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Template> {
            return TemplateType
        }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
    }

    private data class Value(
        override val showInTooltip: Boolean,
    ) : Unbreakable {
        private companion object
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unbreakable> {
        override fun read(holder: ItemComponentHolder): Unbreakable? {
            val itemMeta = holder.item.itemMeta ?: return null
            if (!itemMeta.isUnbreakable) {
                return null
            }
            return Value(!itemMeta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE))
        }

        override fun write(holder: ItemComponentHolder, value: Unbreakable) {
            holder.item.editMeta {
                it.isUnbreakable = true
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { it.isUnbreakable = false }
        }

        private companion object
    }

    data class Template(
        val showInTooltip: Boolean,
    ) : ItemTemplate<Unbreakable> {
        override val componentType: ItemComponentType<Unbreakable> = ItemComponentTypes.UNBREAKABLE

        override fun generate(context: GenerationContext): GenerationResult<Unbreakable> {
            return GenerationResult.of(Value(showInTooltip))
        }
    }

    private data object TemplateType : ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return Template(showInTooltip)
        }
    }
}