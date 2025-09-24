package cc.mewcraft.wakame.registry.entry

interface RegistryEntryOwner<T> {
    fun ownerEquals(other: RegistryEntryOwner<T>): Boolean = other == this
}
