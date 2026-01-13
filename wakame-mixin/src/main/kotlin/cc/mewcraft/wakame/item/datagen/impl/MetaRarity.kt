package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.rarity.LevelToRarityMapping
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

interface MetaRarity : ItemMetaEntry<RegistryEntry<Rarity>> {

    companion object {

        @JvmField
        val SERIALIZER: SimpleSerializer<MetaRarity> = DispatchingSerializer.createPartial<String, MetaRarity>(
            mapOf(
                "constant" to Constant::class,
                "contextual" to Contextual::class,
            )
        )
    }

    override fun write(value: RegistryEntry<Rarity>, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.RARITY, value)
    }

    @ConfigSerializable
    data class Constant(
        @Setting("value")
        val entry: RegistryEntry<Rarity>,
    ) : MetaRarity {

        override fun randomized(): Boolean {
            return false
        }

        override fun make(context: ItemGenerationContext): ItemMetaResult<RegistryEntry<Rarity>> {
            context.rarity = entry
            return ItemMetaResult.of(entry)
        }
    }

    @ConfigSerializable
    data class Contextual(
        @Setting("value")
        val entry: RegistryEntry<LevelToRarityMapping>,
    ) : MetaRarity {

        override fun randomized(): Boolean {
            return true
        }

        override fun make(context: ItemGenerationContext): ItemMetaResult<RegistryEntry<Rarity>> {
            val mapper = entry.unwrap()
            val level = context.level
            if (mapper.contains(level)) {
                val result = mapper.pick(level, context.random)
                context.rarity = result
                return ItemMetaResult.of(result)
            } else {
                LOGGER.warn("Generating no rarity from context: $context. This is possibly a configuration error! ")
                return ItemMetaResult.empty()
            }
        }
    }
}