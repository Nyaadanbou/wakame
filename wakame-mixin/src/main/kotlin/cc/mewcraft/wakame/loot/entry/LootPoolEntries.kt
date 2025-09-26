package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.typeTokenOf

object LootPoolEntries {

    @JvmField
    val ALTERNATIVES: LootPoolEntryType<AlternativesEntry<*>> = register("alternatives", AlternativesEntry.SERIALIZER)

    @JvmField
    val EMPTY: LootPoolEntryType<EmptyLoot> = register("empty", EmptyLoot.SERIALIZER)

    @JvmField
    val GROUP: LootPoolEntryType<EntryGroup<*>> = register("group", EntryGroup.SERIALIZER)

    @JvmField
    val LOOT_TABLE: LootPoolEntryType<NestedLootTable<*>> = register("loot_table", NestedLootTable.SERIALIZER)

    @JvmField
    val SEQUENCE: LootPoolEntryType<SequentialEntry<*>> = register("sequence", SequentialEntry.SERIALIZER)

    @JvmField
    val SIMPLE: LootPoolEntryType<SimpleEntry<*>> = register("simple", SimpleEntry.SERIALIZER)

    private inline fun <reified T : ComposableEntryContainer<*>> register(name: String, serializer: TypeSerializer2<T>): LootPoolEntryType<T> {
        val type = LootPoolEntryType.create(typeTokenOf<T>(), serializer)
        return Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, Identifiers.of(name), type)
    }
}