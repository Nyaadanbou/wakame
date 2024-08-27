package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.display2.RendererSystemName
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.components.cells.template.TemplateCoreSerializer
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.skill.SKILL_EXTERNALS
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

data class PortableCore(
    /**
     * 本便携式核心所包含的核心.
     */
    override val wrapped: Core,
    /**
     * 本便携式核心当前的惩罚值.
     *
     * 该值的具体作用由实现决定, 这里仅提供一个通用的字段.
     */
    val penalty: Int,
) : PortableObject<Core>, Examinable, TooltipProvider.Single {
    companion object : ItemComponentBridge<PortableCore>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<PortableCore> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.PORTABLE_CORE
        override val tooltipKey: Key = ItemConstants.createKey { PORTABLE_CORE }

        private val config = ItemComponentConfig.provide(this)
    }

    override fun provideTooltipLore(systemName: RendererSystemName): LoreLine {
        if (!config.showInTooltip) {
            return LoreLine.noop()
        }
        return wrapped.provideTooltipLore(systemName)
    }

    private data class Codec(override val id: String) : ItemComponentType<PortableCore> {
        override fun read(holder: ItemComponentHolder): PortableCore? {
            val tag = holder.getTag() ?: return null
            val core = tag.getCompoundOrNull(TAG_CORE)?.let { Core.of(it) } ?: return null
            val mergeCount = tag.getInt(TAG_PENALTY)
            return PortableCore(core, mergeCount)
        }

        override fun write(holder: ItemComponentHolder, value: PortableCore) {
            holder.editTag { tag ->
                tag.put(TAG_CORE, value.wrapped.serializeAsTag())
                if (value.penalty > 0) {
                    tag.putInt(TAG_PENALTY, value.penalty)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        companion object {
            private const val TAG_CORE = "core"
            private const val TAG_PENALTY = "penalty"
        }
    }

    data class Template(
        val templateCore: TemplateCore,
    ) : ItemTemplate<PortableCore> {
        override val componentType: ItemComponentType<PortableCore> = ItemComponentTypes.PORTABLE_CORE
        override fun generate(context: GenerationContext): GenerationResult<PortableCore> {
            val core = templateCore.generate(context)
            val portableCore = PortableCore(core, 0)
            return GenerationResult.of(portableCore)
        }
    }

    private data class TemplateType(override val id: String) : ItemTemplateType<Template>, KoinComponent {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <template core>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            val core = node.krequire<TemplateCore>()
            return Template(core)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(TemplateCoreSerializer)
                .registerAll(get(named(SKILL_EXTERNALS))) // 技能, 部分核心会用到
                .build()
        }
    }
}