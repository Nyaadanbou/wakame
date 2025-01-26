package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.KOISH_SCOPE
import cc.mewcraft.wakame.util.coroutine.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.kotlin.MutableCommandBuilder
import org.incendo.cloud.kotlin.coroutines.SuspendingExecutionHandler
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import kotlin.coroutines.CoroutineContext

/**
 * Specify a suspending command execution handler.
 */
fun <C : Any> MutableCommandBuilder<C>.suspendingHandler(
    scope: CoroutineScope = KOISH_SCOPE,
    context: CoroutineContext = Dispatchers.async,
    handler: SuspendingExecutionHandler<C>,
): MutableCommandBuilder<C> = mutate {
    it.suspendingHandler(scope, context, handler)
}

/**
 * Builds this command and adds it to the given [commands].
 *
 * @param C the sender type
 * @param commands the command list
 */
fun <C : CommandSender> MutableCommandBuilder<C>.buildAndAdd(commands: MutableList<Command<out C>>) {
    commands += this.build()
}