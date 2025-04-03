package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

interface MetaKizami : ItemMetaEntry<Set<RegistryEntry<Kizami>>> {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<MetaKizami> = DispatchingSerializer.createPartial<String, MetaKizami>(
            mapOf(
                "static" to Static::class,
                "dynamic" to Dynamic::class,
            )
        )

    }

    // Hint:
    // 尽管 MetaKizami 有多个实现, 但其 write 函数体都是一样的.
    // 因此可以直接在接口定义 write 函数的实现,
    // 其余的实现类只需要重写 make 函数即可.
    override fun write(value: Set<RegistryEntry<Kizami>>, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.KIZAMI, value)
    }

    @ConfigSerializable
    data class Static(
        @Setting("value")
        val entries: Set<RegistryEntry<Kizami>>,
    ) : MetaKizami {

        override fun make(context: Context): ItemMetaResult<Set<RegistryEntry<Kizami>>> {
            return ItemMetaResult.of(entries)
        }

    }

    // TODO #373: 实现动态生成

    @ConfigSerializable
    data class Dynamic(
        @Setting("value")
        val selector: Nothing,
    ) : MetaKizami {

        override fun make(context: Context): ItemMetaResult<Set<RegistryEntry<Kizami>>> {
            TODO("Not yet implemented")
        }

    }

}