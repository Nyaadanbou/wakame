@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.reloader

import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher

internal sealed class ReloaderRunnable<S : ReloaderRunnable<S>> {

    val completion = CompletableDeferred<Unit>()
    abstract val dispatcher: CoroutineDispatcher?

    abstract fun loadDependencies(all: Set<S>, graph: MutableGraph<S>)

    abstract suspend fun run()
}