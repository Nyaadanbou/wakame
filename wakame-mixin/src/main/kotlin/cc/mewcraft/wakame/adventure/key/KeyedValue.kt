package cc.mewcraft.wakame.adventure.key

import net.kyori.adventure.key.KeyedValue

interface KeyedValue<T : Any> : KeyedValue<T> {
    /**
     * @see value
     */
    val value: T

    override fun value(): T = value
}