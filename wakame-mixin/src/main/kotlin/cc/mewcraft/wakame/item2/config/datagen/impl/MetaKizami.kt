package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.context.ItemGenerationContext
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

sealed interface MetaKizami : ItemMetaEntry<Set<RegistryEntry<Kizami>>> {

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
    override fun write(value: Set<RegistryEntry<Kizami>>, itemStack: MojangStack) {
        itemStack.ensureSetData(ItemDataTypes.KIZAMI, value)
    }

    @ConfigSerializable
    data class Static(
        @Setting("value")
        val entries: Set<RegistryEntry<Kizami>>,
    ) : MetaKizami {

        override fun make(context: ItemGenerationContext): ItemMetaResult<Set<RegistryEntry<Kizami>>> {
            return ItemMetaResult.of(entries)
        }

    }

    @ConfigSerializable
    data class Dynamic(
        @Setting("value")
        val selector: LootTable<RegistryEntry<Kizami>>,
    ) : MetaKizami {

        override fun make(context: ItemGenerationContext): ItemMetaResult<Set<RegistryEntry<Kizami>>> {
            return ItemMetaResult.of(selector.select(context).toSet())
        }

    }

}