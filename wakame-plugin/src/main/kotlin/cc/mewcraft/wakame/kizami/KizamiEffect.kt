package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.user.User

/**
 * Represents a collection of potential effects provided by a kizami.
 *
 * The same kizami of different amount can provide a different [KizamiEffect].
 *
 * @see KizamiInstance
 */
sealed interface KizamiEffect {
    /**
     * The collection of effects, such as attributes (modifiers) and skills.
     */
    val effects: List<Single<*>>

    /**
     * Applies the [kizami effects][effects] to the [user].
     */
    fun apply(/* kizami: Kizami, */ user: User<*>) {
        effects.forEach { it.apply(/* kizami, */ user) }
    }

    /**
     * Removes the [kizami effects][effects] from the [user].
     */
    fun remove(/* kizami: Kizami, */ user: User<*>) {
        effects.forEach { it.remove(/* kizami, */ user) }
    }

    /**
     * A single effect.
     *
     * @param T the effect type
     */
    interface Single<T> {
        /**
         * A single effect.
         */
        val effect: T

        /**
         * Applies the [single effect][effect] to the [user].
         */
        fun apply(/* kizami: Kizami, */ user: User<*>)

        /**
         * Removes the [single effect][effect] from the [user].
         */
        fun remove(/* kizami: Kizami, */ user: User<*>)
    }
}
