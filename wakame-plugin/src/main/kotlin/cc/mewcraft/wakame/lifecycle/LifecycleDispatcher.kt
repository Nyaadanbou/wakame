package cc.mewcraft.wakame.lifecycle

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Defines how a Lifecycle Task is dispatched.
 */
enum class LifecycleDispatcher(internal val dispatcher: CoroutineDispatcher?) {

    /**
     * The lifecycle task is performed synchronously with other lifecycle tasks.
     */
    SYNC(null),

    /**
     * The lifecycle task is performed asynchronously, in parallel with other async lifecycle tasks.
     */
    ASYNC(Dispatchers.Default)

}