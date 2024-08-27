package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.template.TemplateCurse
import cc.mewcraft.wakame.item.components.cells.template.TemplateCurseSerializer
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data class PortableCurse(
    override val wrapped: Curse,
) : PortableObject<Curse>, Examinable, TooltipProvider.Single {
    companion object : ItemComponentBridge<PortableCurse>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<PortableCurse> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.PORTABLE_CURSE
        override val tooltipKey: Key = ItemComponentConstants.createKey { PORTABLE_CURSE }

        private val config = ItemComponentConfig.provide(this)
    }

    override fun provideTooltipLore(): LoreLine {
        return LoreLine.noop()
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<PortableCurse> {
        override fun read(holder: ItemComponentHolder): PortableCurse? {
            val tag = holder.getTag() ?: return null
            val curse = tag.getCompoundOrNull(TAG_CURSE)?.let { Curse.of(it) } ?: return null
            return PortableCurse(curse)
        }

        override fun write(holder: ItemComponentHolder, value: PortableCurse) {
            holder.editTag { tag ->
                tag.put(TAG_CURSE, value.wrapped.serializeAsTag())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        companion object {
            const val TAG_CURSE = "curse"
        }
    }

    data class Template(
        val curse: TemplateCurse,
    ) : ItemTemplate<PortableCurse> {
        override val componentType: ItemComponentType<PortableCurse> = ItemComponentTypes.PORTABLE_CURSE

        override fun generate(context: GenerationContext): GenerationResult<PortableCurse> {
            val curse = curse.generate(context)
            val portableCurse = PortableCurse(curse)
            return GenerationResult.of(portableCurse)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template>, KoinComponent {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <template curse>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            val templateCurse = node.krequire<TemplateCurse>()
            return Template(templateCurse)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(TemplateCurseSerializer)
                .registerAll(get(named(ENTITY_TYPE_HOLDER_EXTERNALS))) // 实体类型, 部分诅咒会用到
                .build()
        }
    }
}
