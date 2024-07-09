package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
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

interface HideAdditionalTooltip : Examinable {

    companion object : ItemComponentBridge<HideAdditionalTooltip>, ItemComponentMeta {
        fun of(): HideAdditionalTooltip {
            return Value
        }

        override fun codec(id: String): ItemComponentType<HideAdditionalTooltip> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Template> {
            return TemplateType
        }

        override val configPath: String = ItemComponentConstants.HIDE_ADDITIONAL_TOOLTIP
        override val tooltipKey: Key = ItemComponentConstants.createKey { HIDE_ADDITIONAL_TOOLTIP }
    }

    private data object Value : HideAdditionalTooltip

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<HideAdditionalTooltip> {
        override fun read(holder: ItemComponentHolder): HideAdditionalTooltip? {
            val im = holder.item.itemMeta ?: return null
            if (im.hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)) {
                return Value
            }
            return null
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
        }

        override fun write(holder: ItemComponentHolder, value: HideAdditionalTooltip) {
            holder.item.editMeta {
                it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
        }
    }

    data object Template : ItemTemplate<HideAdditionalTooltip> {
        override val componentType: ItemComponentType<HideAdditionalTooltip> = ItemComponentTypes.HIDE_ADDITIONAL_TOOLTIP

        override fun generate(context: GenerationContext): GenerationResult<HideAdditionalTooltip> {
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