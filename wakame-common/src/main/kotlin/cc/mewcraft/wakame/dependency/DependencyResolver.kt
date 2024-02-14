@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.dependency

import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph

class CircularDependencyException(
    message: String,
) : RuntimeException(message)

object DependencyResolver {

    /**
     * @throws CircularDependencyException
     */
    fun <T : Any> resolveDependencies(nodes: List<DependencyComponent<T>>): List<T> {
        val graph: MutableGraph<T> = GraphBuilder.directed().allowsSelfLoops(false).build()

        nodes.forEach { node ->
            graph.addNode(node.component)
            node.dependenciesAfter.forEach { dependency ->
                graph.putEdge(node.component, dependency) // node.component depends on dependency
            }
            node.dependenciesBefore.forEach { dependency ->
                graph.putEdge(dependency, node.component) // dependency depends on node.component
            }
        }

        return topologicalSort(graph)
    }

    /**
     * @throws CircularDependencyException
     */
    private fun <T : Any> topologicalSort(graph: Graph<T>): List<T> {
        val visited = mutableSetOf<T>()
        val tempMarks = mutableSetOf<T>()
        val pathStack = mutableListOf<T>()
        val result = mutableListOf<T>()

        fun visit(node: T) {
            if (node in tempMarks) {
                val cycleStartIndex = pathStack.indexOf(node)
                val cycle = pathStack.subList(cycleStartIndex, pathStack.size).joinToString(" -> ")
                throw CircularDependencyException("Detected circular dependency: $cycle -> $node")
            }
            if (node !in visited) {
                tempMarks.add(node)
                pathStack.add(node)
                graph.successors(node).forEach {
                    visit(it)
                }
                pathStack.remove(node)
                tempMarks.remove(node)
                visited.add(node)
                result.add(node)
            }
        }

        graph.nodes().forEach {
            if (it !in visited) {
                visit(it)
            }
        }

        return result.reversed()
    }

}
