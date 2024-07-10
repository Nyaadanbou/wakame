package cc.mewcraft.wakame.crate

/**
 * 代表一个物品形式的盲盒，也就是存在于世界里的。
 */
class ItemCrate : Crate {
    override val level: Int = 1
}

/**
 * 代表一个虚拟的盲盒，也就是不存在于世界里的。
 */
class VirtualCrate : Crate {
    override val level: Int = 1
}