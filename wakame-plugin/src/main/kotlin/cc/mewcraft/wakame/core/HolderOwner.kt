package cc.mewcraft.wakame.core

interface HolderOwner<T> {
    fun canSerialize(other: HolderOwner<T>): Boolean = other == this
}
