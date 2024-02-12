package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.condition.Condition
import org.spongepowered.configurate.ConfigurationNode
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
 *   selectors: <children map>
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
            node.node("filters").childrenList().forEach {
                this.conditions += conditionFactory(it)
            }

            node.node("selectors").childrenMap().forEach { (poolName, poolNode) ->
                this.pools[poolName.toString()] = poolFactory(poolNode)
            }

            this.default = poolFactory(node.node("default"))
        }
    }
}