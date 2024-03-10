package cc.mewcraft.wakame.adventure

import net.kyori.adventure.key.KeyedValue

interface KeyedValue<T : Any> : KeyedValue<T> {
    /**
     * @see value
     */
    val value: T

    override fun value(): T = value
}