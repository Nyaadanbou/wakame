package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.condition.Condition
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import java.lang.reflect.Type


/**
 * Shares the common code between pool serializers.
 *
 * The subclasses must assume that the config structure is the following:
 *
 * ## Node structure
 *
 * ```yaml
 * <node>:
 *   filters: <children list>
 *   selects: <children map>
 *   default: <pool>
 * ```
 *
 * @param S the type of content
 * @param C the type of context
 */
abstract class AbstractGroupSerializer<S, C : SelectionContext> : SchemeSerializer<Group<S, C>> {
    protected abstract fun poolFactory(node: ConfigurationNode): Pool<S, C>
    protected abstract fun conditionFactory(node: ConfigurationNode): Condition<C>

    final override fun deserialize(type: Type, node: ConfigurationNode): Group<S, C> {
        return Group.build {
            // can be omitted completely in config
            node.node("filters").childrenList().forEach {
                this.conditions += conditionFactory(it)
            }

            // can be omitted completely in config
            node.node("selects").childrenMap().mapKeys { it.key.toString() }.forEach { (poolName, localPoolNode) ->
                val rawScalar = localPoolNode.rawScalar()
                if (rawScalar != null) {
                    // it's a raw string, meaning it's referencing a node in shared pools,
                    // so we need to pass the external node to the factory function
                    val sharedPoolsNode = requireNotNull(node.ownHint(SHARED_POOLS)) { "No hint is provided for node ${node.key()}" }
                    val externalPoolNode = sharedPoolsNode.node(rawScalar)
                    this.pools[poolName] = poolFactory(externalPoolNode)
                } else {
                    // it's not a raw string - we just pass the local node
                    this.pools[poolName] = poolFactory(localPoolNode)
                }
            }

            // can be omitted completely in config
            val defaultNode = node.node("default")
            this.default = if (defaultNode.virtual()) Pool.empty() else poolFactory(defaultNode)
        }
    }

    companion object Constants {
        val SHARED_POOLS: RepresentationHint<ConfigurationNode> = RepresentationHint.of("shared_pools", ConfigurationNode::class.java)
    }
}