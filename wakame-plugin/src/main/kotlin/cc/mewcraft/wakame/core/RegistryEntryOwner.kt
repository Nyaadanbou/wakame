package cc.mewcraft.wakame.core

interface RegistryEntryOwner<T> {
    fun ownerEquals(other: RegistryEntryOwner<T>): Boolean = other == this
}
