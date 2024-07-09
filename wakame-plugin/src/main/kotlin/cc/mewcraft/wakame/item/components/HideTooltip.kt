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
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface HideTooltip : Examinable {

    companion object : ItemComponentBridge<HideTooltip>, ItemComponentMeta {
        fun of(): HideTooltip {
            return Value
        }

        override fun codec(id: String): ItemComponentType<HideTooltip> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Template> {
            return TemplateType
        }

        override val configPath: String = ItemComponentConstants.HIDE_TOOLTIP
        override val tooltipKey: Key = ItemComponentConstants.createKey { HIDE_TOOLTIP }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
    }

    private data object Value : HideTooltip

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<HideTooltip> {
        override fun read(holder: ItemComponentHolder): HideTooltip? {
            val im = holder.item.itemMeta ?: return null
            if (im.isHideTooltip) {
                return Value
            }
            return null
        }

        override fun write(holder: ItemComponentHolder, value: HideTooltip) {
            holder.item.editMeta {
                it.isHideTooltip = true
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.isHideTooltip = false
            }
        }
    }

    data object Template : ItemTemplate<HideTooltip> {
        override val componentType: ItemComponentType<HideTooltip> = ItemComponentTypes.HIDE_TOOLTIP

        override fun generate(context: GenerationContext): GenerationResult<HideTooltip> {
            return GenerationResult.of(Value)
        }
    }

    private data object TemplateType : ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return Template
        }
    }

}