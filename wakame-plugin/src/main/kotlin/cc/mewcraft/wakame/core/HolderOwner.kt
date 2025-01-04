package cc.mewcraft.wakame.core

interface HolderOwner<T> {
    fun canSerializeIn(other: HolderOwner<T>): Boolean = other == this
}
