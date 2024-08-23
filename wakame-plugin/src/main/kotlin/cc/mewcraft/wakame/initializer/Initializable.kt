package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import me.lucko.helper.terminable.Terminable

/**
 * Something that should be initialized.
 *
 * Each of the member functions will be called by the [Initializer] at
 * certain time as documented in the function kdoc. The exact timings are
 * described below:
 * - Functions of different stages will never be called "simultaneously".
 *   For example, [onPreWorld] will never be called while [onPostWorld] is
 *   being called, and vice versa.
 * - Functions of different stages will always be called in the order as
 *   they are declared in this file. For example, [onPreWorld] is always
 *   called before [onPostWorld] because [onPreWorld] is declared **before**
 *   [onPostWorld]. You can rely on this rule to enforce a basic
 *   initialization order.
 * - However, functions of the same stage will be called in
 *   un-deterministic order. Suppose there are 2 initializables, X and Y.
 *   The [onPreWorld] of class X makes no guaranteed to be called before or
 *   after that of class Y. To enforce the invocation order, you need to
 *   add a DependencyConfiguration annotation on your
 *   [initializable][Initializable] class.
 *
 * **Note for implementations**: you must also declare your implementation
 * class in your Koin module, or else your functions won't be called by the
 * [Initializer].
 */
interface Initializable : Terminable {
    /**
     * Before the world is loaded.
     */
    fun onPreWorld() {}

    /**
     * After the world has been loaded.
     */
    fun onPostWorld() {}

    /**
     * After the world has been loaded, in an async thread.
     */
    suspend fun onPostWorldAsync() {}

    /**
     * When [NekoCommandReloadEvent] is fired.
     */
    fun onReload() {}

    /**
     * When [NekoCommandReloadEvent] is fired, in an async thread.
     */
    suspend fun onReloadAsync() {}

    /**
     * Closes this terminable.
     */
    override fun close() {}
}