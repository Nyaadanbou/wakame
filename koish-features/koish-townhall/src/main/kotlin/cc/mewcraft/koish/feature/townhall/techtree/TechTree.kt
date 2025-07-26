package cc.mewcraft.koish.feature.townhall.techtree

import cc.mewcraft.koish.feature.townhall.component.EnhancementType
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import com.palmergames.bukkit.towny.`object`.Town

class TechTree {
    companion object {
        val SERIALIZER: TypeSerializer2<TechTree> = TypeSerializer2 { type, node ->
            val techNodes = node.node("nodes").require<List<TechNode>>()
            val tree = TechTree()
            techNodes.forEach { tree.addNode(it) }
            for (techNode in techNodes) {
                for (dependency in techNode.dependencies) {
                    val depNode = techNodes.find { it.enhancement == dependency }
                        ?: throw IllegalArgumentException("Dependency $dependency not found for node ${techNode.enhancement}")
                    tree.addDependency(depNode, techNode)
                }
            }
            tree
        }
    }

    private val nodes: MutableMap<EnhancementType, TechNode> = mutableMapOf()

    private val graph: MutableGraph<TechNode> =
        GraphBuilder.directed().allowsSelfLoops(false).build()

    private fun addNode(node: TechNode) {
        if (nodes.containsKey(node.enhancement)) {
            throw IllegalArgumentException("Node with enhancement ${node.enhancement} already exists.")
        }
        graph.addNode(node)
        nodes[node.enhancement] = node
    }

    private fun addDependency(from: TechNode, to: TechNode) {
        graph.putEdge(from, to)
    }

    fun canUnlock(town: Town, enhancementType: EnhancementType): Boolean {
        val node = nodes[enhancementType] ?: return false
        if (node.isUnlocked(town)) return false
        val predecessors = graph.predecessors(node)
        return predecessors.all { it.isUnlocked(town) }
    }

    fun availableNodes(town: Town): List<TechNode> =
        graph.nodes().filter { canUnlock(town, it.enhancement) }

    fun unlock(town: Town, enhancementType: EnhancementType): Boolean {
        if (!canUnlock(town, enhancementType)) return false
        val node = nodes[enhancementType] ?: return false
        node.setUnlocked(town, true)
        return true
    }
}

