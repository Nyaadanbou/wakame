package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.CoreContainer
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

interface MetaCoreContainer : ItemMetaEntry<CoreContainer> {

    companion object {
        @JvmField
        val SERIALIZER: TypeSerializer2<MetaCoreContainer> = DispatchingSerializer.createPartial<String, MetaCoreContainer>(
            mapOf(
                "static" to Static::class,
                "dynamic" to Dynamic::class,
            )
        )
    }

    override fun write(value: CoreContainer, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.CORE_CONTAINER, value)
    }

    @ConfigSerializable
    data class Static(
        @Setting("value")
        val entry: CoreContainer,
    ) : MetaCoreContainer {
        override fun make(context: Context): ItemMetaResult<CoreContainer> {
            return ItemMetaResult.of(entry)
        }
    }

    @ConfigSerializable
    data class Dynamic(
        @Setting("value")
        val entry: Nothing,
    ) : MetaCoreContainer {
        override fun make(context: Context): ItemMetaResult<CoreContainer> {
            // TODO #373: 实现动态生成
            return ItemMetaResult.empty()
        }
    }

}