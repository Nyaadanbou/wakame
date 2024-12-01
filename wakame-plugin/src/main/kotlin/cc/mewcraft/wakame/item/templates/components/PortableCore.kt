package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetype
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetypeSerializer
import cc.mewcraft.wakame.skill.SKILL_EXTERNALS
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import cc.mewcraft.wakame.item.components.PortableCore as PortableCoreData

data class PortableCore(
    val coreArchetype: CoreArchetype,
) : ItemTemplate<PortableCoreData> {
    override val componentType: ItemComponentType<PortableCoreData> = ItemComponentTypes.PORTABLE_CORE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<PortableCoreData> {
        val core = coreArchetype.generate(context)
        val portableCore = PortableCoreData(core, 0)
        return ItemGenerationResult.of(portableCore)
    }

    companion object : ItemTemplateBridge<PortableCore> {
        override fun codec(id: String): ItemTemplateType<PortableCore> {
            return Codec(id)
        }
    }

    private data class Codec(override val id: String) : ItemTemplateType<PortableCore>, KoinComponent {
        override val type: TypeToken<PortableCore> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <template core>
         * ```
         */
        override fun decode(node: ConfigurationNode): PortableCore {
            val core = node.krequire<CoreArchetype>()
            return PortableCore(core)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(CoreArchetypeSerializer)
                .registerAll(get(named(SKILL_EXTERNALS))) // 技能, 部分核心会用到
                .build()
        }
    }
}