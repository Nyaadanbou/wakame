package cc.mewcraft.wakame.registry2.entry

interface RegistryEntryOwner<T> {
    fun ownerEquals(other: RegistryEntryOwner<T>): Boolean = other == this
}
