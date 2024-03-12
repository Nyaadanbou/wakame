package cc.mewcraft.wakame.crate

/**
 * Represents a crate in the game.
 *
 * Currently, it's usually backed by an item, but it also may be backed by
 * any other object that makes senses to our game design.
 */
interface Crate {
    val level: Int
}
