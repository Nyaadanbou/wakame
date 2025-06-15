package cc.mewcraft.wakame.random4.entry

import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.Registry
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object LootPoolEntries {
    @JvmField
    val ALTERNATIVES: LootPoolEntryType<AlternativesEntry<*>> = register("alternatives")

    @JvmField
    val EMPTY: LootPoolEntryType<EmptyLoot> = register("empty")

    @JvmField
    val GROUP: LootPoolEntryType<EntryGroup<*>> = register("group")

    @JvmField
    val SEQUENCE: LootPoolEntryType<SequentialEntry<*>> = register("sequence")

    private inline fun <reified T : ComposableEntryContainer<*>> register(name: String, block: LootPoolEntryType.Builder<T>.() -> Unit = {}): LootPoolEntryType<T> {
        val type = LootPoolEntryType.builder(typeTokenOf<T>()).apply(block).build()
        return Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, Identifiers.of(name), type)
    }

    internal fun directSerializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }
}