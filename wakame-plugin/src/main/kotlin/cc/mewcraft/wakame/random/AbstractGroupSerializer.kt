package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.util.javaTypeOf
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.contains
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type


/**
 * Shares the common code between pool serializers.
 *
 * The subclasses must assume that the config structure is the following:
 *
 * ## Node structure 1
 *
 * This structure can have all the components specified.
 *
 * ```yaml
 * <node>:
 *   filters: <children list>
 *   selects: <children map>
 *   default: <pool>
 * ```
 *
 * ## Node structure 2
 *
 * This structure only has the `selects` component.
 * This could be useful if neither `filters` nor `default` are needed.
 *
 * ```yaml
 * <node>:
 *   <children map>
 * ```
 *
 * @param S the type of content
 * @param C the type of context
 */
abstract class AbstractGroupSerializer<S, C : SelectionContext> : SchemeSerializer<Group<S, C>> {
    companion object Constants {
        val SHARED_POOLS: RepresentationHint<ConfigurationNode> = RepresentationHint.of("shared_pools", ConfigurationNode::class.java)

        private const val FILTERS_PATH = "filters"
        private const val SELECTS_PATH = "selects"
        private const val DEFAULT_PATH = "default"
    }

    protected abstract fun poolFactory(node: ConfigurationNode): Pool<S, C>
    protected abstract fun conditionFactory(node: ConfigurationNode): Condition<C>

    final override fun deserialize(type: Type, node: ConfigurationNode): Group<S, C> {
        return Group.build {
            when {
                // Node structure 1
                node.isMap && (!node.contains(FILTERS_PATH) && !node.contains(SELECTS_PATH) && !node.contains(DEFAULT_PATH)) -> {
                    // it's a list, which means it only has pools (no filters, no default)

                    deserializeSelects(node, node)
                }

                // Node structure 2
                node.isMap -> {
                    // it's a map, which means it might have all components specified

                    node.node(FILTERS_PATH).childrenList().forEach { this.conditions += conditionFactory(it) }
                    node.node(SELECTS_PATH).run { deserializeSelects(node, this) }
                    node.node(DEFAULT_PATH).run {
                        if (this.virtual()) {
                            Pool.empty()
                        } else {
                            poolFactory(this)
                        }
                    }.also {
                        this.default = it
                    }
                }

                // Unknown structure
                else -> {
                    // it's an unknown format

                    throw SerializationException(node.path(), type, "Unsupported format")
                }
            }
        }
    }

    private fun GroupBuilder<S, C>.deserializeSelects(groupNode: ConfigurationNode, selectsNode: ConfigurationNode) {
        selectsNode.childrenMap().mapKeys { it.key.toString() }.forEach { (poolName, localPoolNode) ->
            val rawScalar = localPoolNode.rawScalar()
            if (rawScalar != null) {
                // it's a raw string, meaning it's referencing a node in shared pools,
                // so we need to pass the external node to the factory function
                val sharedPoolsNode = groupNode.ownHint(SHARED_POOLS) ?: throw SerializationException(selectsNode, javaTypeOf<Group<S, C>>(), "No hint is provided for node '${selectsNode.key()}'")
                val externalPoolNode = sharedPoolsNode.node(rawScalar)
                this.pools[poolName] = poolFactory(externalPoolNode)
            } else {
                // it's not a raw string - we just pass the local node
                this.pools[poolName] = poolFactory(localPoolNode)
            }
        }
    }
}