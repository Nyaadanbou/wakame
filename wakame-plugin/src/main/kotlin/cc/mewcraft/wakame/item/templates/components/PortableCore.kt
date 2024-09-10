package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.components.cells.CoreBlueprint
import cc.mewcraft.wakame.item.templates.components.cells.CoreBlueprintSerializer
import cc.mewcraft.wakame.skill.SKILL_EXTERNALS
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import cc.mewcraft.wakame.item.components.PortableCore as PortableCoreData

data class PortableCore(
    val coreBlueprint: CoreBlueprint,
) : ItemTemplate<PortableCoreData> {
    override val componentType: ItemComponentType<PortableCoreData> = ItemComponentTypes.PORTABLE_CORE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<PortableCoreData> {
        val core = coreBlueprint.generate(context)
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
            val core = node.krequire<CoreBlueprint>()
            return PortableCore(core)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(CoreBlueprintSerializer)
                .registerAll(get(named(SKILL_EXTERNALS))) // 技能, 部分核心会用到
                .build()
        }
    }
}