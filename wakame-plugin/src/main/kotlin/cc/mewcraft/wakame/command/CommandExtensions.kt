package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.PLUGIN_SCOPE
import cc.mewcraft.wakame.util.coroutine.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.incendo.cloud.kotlin.MutableCommandBuilder
import org.incendo.cloud.kotlin.coroutines.SuspendingExecutionHandler
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import kotlin.coroutines.CoroutineContext

/**
 * Specify a suspending command execution handler.
 */
fun <C : Any> MutableCommandBuilder<C>.koishHandler(
    scope: CoroutineScope = PLUGIN_SCOPE,
    context: CoroutineContext = Dispatchers.async,
    handler: SuspendingExecutionHandler<C>,
): MutableCommandBuilder<C> = mutate {
    it.suspendingHandler(scope, context, handler)
}
