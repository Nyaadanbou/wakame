package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.util.typedRequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type


/**
 * Shares the common code between pool serializers.
 *
 * The subclasses must assume that the config structure is either of the
 * following:
 *
 * ## Node structure 1 (simple)
 *
 * ```yaml
 * <node>:
 *    - <impl_defined>: <impl_defined>
 *      weight: 1
 *      filters: # optional
 *        - type: <condition_type>
 *          <impl_defined>: <impl_defined>
 *          ...
 *        - type: <condition_type>
 *          <impl_defined>: <impl_defined>
 *          ...
 *      mark: x1y1z1 # optional
 *    - <node with the same format as above>
 *    - ...
 * ```
 *
 * ## Node structure 2 (completed)
 *
 * ```yaml
 * <node>:
 *   sample: 1 # optional
 *   replacement: false # optional
 *   filters: # optional
 *     - type: <condition_type>
 *       <impl_defined>: <impl_defined>
 *       ...
 *     - <node with the same format as above>
 *     - ...
 *   entries:
 *     - <impl_defined>: <impl_defined>
 *       weight: 1
 *       filters: # optional
 *         - type: <condition_type>
 *           <impl_defined>: <impl_defined>
 *           ...
 *         - <node with the same format as above>
 *         - ...
 *       mark: x1y1z1 # optional
 *     - <node with the same format as above>
 *     - ...
 * ```
 *
 * @param S the type of content
 * @param C the type of context
 */
abstract class AbstractPoolSerializer<S, C : SelectionContext> : SchemeSerializer<Pool<S, C>> {

    // region Subclasses must implement this member
    /**
     * The factory to create content [S] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * <node>:
     *   <impl_defined>: <impl_defined>
     *   ...
     *   <impl_defined>: <impl_defined>
     *   weight: 1
     *   filters: # optional
     *     - type: <condition_type>
     *       <impl_defined>: <impl_defined>
     *     - <node with the same format as above>
     *     - ...
     *   mark: x1y1z1 # optional
     * ```
     *
     * The `<impl_defined>` is what you need to take care of.
     *
     * @param node the configuration node
     * @return the content
     */
    protected abstract fun contentFactory(node: ConfigurationNode): S
    // endregion

    // region Subclasses may optionally override these members
    /**
     * The factory to create [Condition] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * type: <condition_type>
     * <impl_defined>: <impl_defined>
     *     ...
     * <impl_defined>: <impl_defined>
     * ```
     *
     * @param node the configuration node
     * @return a new condition
     */
    protected open fun conditionFactory(node: ConfigurationNode): Condition<C> = Condition.alwaysTrue()

    /**
     * This function will be applied to the context. You can override this
     * function if your selection needs to leave some "traces" in the context.
     *
     * @param content the content wrapped in the [sample][Sample]
     * @param context the context
     */
    protected open fun traceApply(content: S, context: C) {}

    /**
     * This function will be applied to the [pool builder][Pool.Builder]. You
     * can override this function if you need to specifically tweak the built
     * pool.
     *
     * @param builder the pool builder
     */
    protected open fun builderApply(builder: Pool.Builder<S, C>) {}
    // endregion

    private fun deserializeConditionList(node: ConfigurationNode): List<Condition<C>> {
        // if the node is virtual, the childrenList() will just be an empty list
        return node.childrenList().map(::conditionFactory)
    }

    private fun deserializeSampleList(node: ConfigurationNode): List<Sample<S, C>> {
        return node.childrenList().map { n ->
            // create sample content from the node
            val content = contentFactory(n)

            // wrap it into a sample
            buildSample(content) {
                weight = n.node("weight").typedRequire<Double>()
                conditions += deserializeConditionList(n.node("filters"))
                mark = n.node("mark").string?.let { Mark.stringMarkOf(it) }
                trace = {
                    // add the mark to the context
                    mark?.run { it.marks += this }

                    // apply the given trace function
                    traceApply(content, it)
                }
            }
        }
    }

    final override fun deserialize(type: Type, node: ConfigurationNode): Pool<S, C> {
        if (node.isList) {
            // it's the structure 1

            return Pool.buildPool {
                samples += deserializeSampleList(node)
                // other values are all default

                builderApply(this)
            }
        } else if (node.isMap) {
            // it's the structure 2

            return Pool.buildPool {
                samples += deserializeSampleList(node.node("entries"))
                conditions += deserializeConditionList(node.node("filters"))
                pickCount = node.node("sample").getLong(1)
                isReplacement = node.node("replacement").getBoolean(false)

                builderApply(this)
            }
        } else {
            // it's an illegal structure
            throw SerializationException("Can't serialize pool ${node.path()} due to illegal structure")
        }
    }
}