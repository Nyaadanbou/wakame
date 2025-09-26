package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable

sealed interface MetaElement : ItemMetaEntry<Set<RegistryEntry<Element>>> {
    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<MetaElement> = DispatchingSerializer.createPartial<String, MetaElement>(
            mapOf(
                "static" to Static::class,
                "dynamic" to Dynamic::class,
            )
        )
    }

    override fun write(value: Set<RegistryEntry<Element>>, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.ELEMENT, value)
    }

    @ConfigSerializable
    data class Static(
        val entries: Set<RegistryEntry<Element>>,
    ) : MetaElement {

        override fun make(context: ItemGenerationContext): ItemMetaResult<Set<RegistryEntry<Element>>> {
            return ItemMetaResult.of(entries)
        }

    }

    @ConfigSerializable
    data class Dynamic(
        val entries: LootTable<RegistryEntry<Element>>,
    ) : MetaElement {

        override fun make(context: ItemGenerationContext): ItemMetaResult<Set<RegistryEntry<Element>>> {
            return ItemMetaResult.of(entries.select(context).toSet())
        }

    }
}