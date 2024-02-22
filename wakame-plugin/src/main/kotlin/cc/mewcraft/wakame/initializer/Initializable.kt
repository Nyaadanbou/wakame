package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.event.NekoReloadEvent
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
 *   called before [onPrePack] because [onPreWorld] is declared **before**
 *   [onPrePack]. You can rely on this rule to enforce a basic
 *   initialization order.
 * - However, functions of the same stage will be called in
 *   un-deterministic order. Suppose there are 2 initializables, X and Y.
 *   The [onPreWorld] of class X makes no guaranteed to be called before or
 *   after that of class Y. To enforce the invocation order, you need to
 *   add a DependencyConfigurations annotation on your
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
     * Before the resource pack generation starts.
     */
    fun onPrePack() {}

    /**
     * After the first stage of resource pack generation ("pre-world") has
     * finished. Lookup registries are now loaded.
     */
    fun onPostPackPreWorld() {}

    /**
     * After the world has been loaded.
     */
    fun onPostWorld() {}

    /**
     * After the world has been loaded, in an async thread.
     */
    suspend fun onPostWorldAsync() {}

    /**
     * After the second (and last) stage of resource pack generation
     * ("post-world") has finished.
     */
    fun onPostPack() {}

    /**
     * After the second (and last) stage of resource pack generation
     * ("post-world") has finished, in an async thread.
     */
    suspend fun onPostPackAsync() {}

    /**
     * When [NekoReloadEvent] is fired.
     */
    fun onReload() {}

    /**
     * Closes this terminable.
     */
    override fun close() {}
}