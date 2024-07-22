package cc.mewcraft.wakame.item.components

/**
 * 代表一个“便携式”组件, 一个用于包含其他组件的组件.
 */
interface PortableObject<T> {
    val wrapped: T
}