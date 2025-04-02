package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializers
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

interface MetaRarity : ItemMetaEntry<RegistryEntry<Rarity>> {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<MetaRarity> = TypeSerializers.dispatchPartial<String, MetaRarity>(
            mapOf(
                "static" to Static::class,
                "dynamic" to Dynamic::class,
            )
        )

    }

    @ConfigSerializable
    data class Static(
        @Setting("value")
        val entry: RegistryEntry<Rarity>,
    ) : MetaRarity {
        override fun make(context: Context): ItemMetaResult<RegistryEntry<Rarity>> {
            return ItemMetaResult.of(entry)
        }

        override fun write(value: RegistryEntry<Rarity>, itemstack: MojangStack) {
            itemstack.ensureSetData(ItemDataTypes.RARITY, entry)
        }
    }

    // TODO #373: 迁移 LevelRarityMapping (需要先迁移 helper, 等技能合并

    @ConfigSerializable
    data class Dynamic(
        @Setting("value")
        val mapper: Nothing,
    ) : MetaRarity {
        override fun make(context: Context): ItemMetaResult<RegistryEntry<Rarity>> {
            TODO("Not yet implemented")
        }

        override fun write(value: RegistryEntry<Rarity>, itemstack: MojangStack) {
            TODO("Not yet implemented")
        }
    }

}