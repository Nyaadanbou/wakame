package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.Core
import cc.mewcraft.wakame.item.data.impl.CoreContainer
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

sealed interface MetaCoreContainer : ItemMetaEntry<CoreContainer> {

    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<MetaCoreContainer> = DispatchingSerializer.createPartial<String, MetaCoreContainer>(
            mapOf(
                 "constant" to Constant::class,
                "contextual" to Contextual::class,
            )
        )
    }

    override fun write(value: CoreContainer, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.CORE_CONTAINER, value)
    }

    @ConfigSerializable
    data class Constant(
        @Setting("value")
        val entry: CoreContainer,
    ) : MetaCoreContainer {

        override fun randomized(): Boolean {
            return false
        }

        override fun make(context: ItemGenerationContext): ItemMetaResult<CoreContainer> {
            return ItemMetaResult.of(entry)
        }
    }

    @ConfigSerializable
    data class Contextual(
        @Setting("value")
        val entry: Map<String, LootTable<Core>>,
    ) : MetaCoreContainer {

        override fun randomized(): Boolean {
            return true
        }

        override fun make(context: ItemGenerationContext): ItemMetaResult<CoreContainer> {
            val cores = entry.mapValues { (_, table) -> table.select(context).first() }
            return ItemMetaResult.of(CoreContainer.of(cores))
        }
    }

}