package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.rarity2.LevelToRarityMapping
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

interface MetaRarity : ItemMetaEntry<RegistryEntry<Rarity>> {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<MetaRarity> = DispatchingSerializer.createPartial<String, MetaRarity>(
            mapOf(
                "static" to Static::class,
                "dynamic" to Dynamic::class,
            )
        )

    }

    override fun write(value: RegistryEntry<Rarity>, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.RARITY, value)
    }

    @ConfigSerializable
    data class Static(
        @Setting("value")
        val entry: RegistryEntry<Rarity>,
    ) : MetaRarity {

        override fun make(context: Context): ItemMetaResult<RegistryEntry<Rarity>> {
            return ItemMetaResult.of(entry)
        }

    }

    @ConfigSerializable
    data class Dynamic(
        @Setting("value")
        val entry: RegistryEntry<LevelToRarityMapping>,
    ) : MetaRarity {

        override fun make(context: Context): ItemMetaResult<RegistryEntry<Rarity>> {
            val mapper = entry.unwrap()
            val level = context.level
            if (mapper.contains(level)) {
                return ItemMetaResult.of(mapper.pick(level, context.random))
            } else {
                LOGGER.warn("Generating no rarity from context: $context. This is possibly a configuration error! ")
                return ItemMetaResult.empty()
            }
        }

    }

}