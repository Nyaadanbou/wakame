@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle

import com.google.common.graph.MutableGraph

/**
 * Internal graph utilities.
 */
internal object LifecycleGraph {

    fun <T : Any> tryPutEdge(graph: MutableGraph<T>, from: T, to: T) {
        try {
            graph.putEdge(from, to)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add edge from '$from' to '$to'", e)
        }
    }
}
