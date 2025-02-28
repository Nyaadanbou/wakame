@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle.initializer

import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher

internal sealed class InitializerRunnable<S : InitializerRunnable<S>> {

    val completion = CompletableDeferred<Unit>()

    abstract val dispatcher: CoroutineDispatcher?
    abstract fun loadDependencies(all: Set<S>, graph: MutableGraph<S>)
    abstract suspend fun run()
}