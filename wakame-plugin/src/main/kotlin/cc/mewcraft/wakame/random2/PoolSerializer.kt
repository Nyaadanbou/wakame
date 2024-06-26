package cc.mewcraft.wakame.random2

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.krequire
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
 *        - type: <filter_type>
 *          <impl_defined>: <impl_defined>
 *          ...
 *        - type: <filter_type>
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
 *     - type: <filter_type>
 *       <impl_defined>: <impl_defined>
 *       ...
 *     - <node with the same format as above>
 *     - ...
 *   entries:
 *     - <impl_defined>: <impl_defined>
 *       weight: 1
 *       filters: # optional
 *         - type: <filter_type>
 *           <impl_defined>: <impl_defined>
 *           ...
 *         - <node with the same format as above>
 *         - ...
 *       mark: x1y1z1 # optional
 *     - <node with the same format as above>
 *     - ...
 * ```
 *
 * @param S the type of sample
 * @param C the type of context
 */
abstract class PoolSerializer<S, C : SelectionContext> : SchemaSerializer<Pool<S, C>> {

    /**
     * The factory to create content [S] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * <node>:
     *   <impl_defined>: <impl_defined>
     *   ...
     *   weight: 1
     *   mark: x1y1z1 # optional
     *   filters: # optional
     *     - type: <filter_type>
     *       <impl_defined>: <impl_defined>
     *     - <node with the same format as above>
     *     - ...
     * ```
     *
     * The `<impl_defined>` is what you need to take care of.
     *
     * @param node the configuration node
     * @return the content
     */
    protected abstract fun sampleFactory(node: ConfigurationNode): S

    /**
     * **Subclasses may optionally override this.**
     *
     * The factory to create [Filter] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * type: <filter_type>
     * <impl_defined>: <impl_defined>
     *     ...
     * <impl_defined>: <impl_defined>
     * ```
     *
     * @param node the configuration node
     * @return a new filter
     */
    protected open fun filterFactory(node: ConfigurationNode): Filter<C> = Filter.alwaysTrue()

    /**
     * **Subclasses may optionally override this.**
     *
     * Defines the "intrinsic filters" of each sample in the pool.
     *
     * "Intrinsic filters" can be thought as those which will be
     * automatically added to the sample without specifically configuring
     * it in the configuration.
     *
     * @return the intrinsic filters
     */
    protected open fun intrinsicFilters(content: S): Filter<C> = Filter.alwaysTrue()

    /**
     * **Subclasses may optionally override this.**
     *
     * This function will be called upon the sample is picked.
     *
     * You can override this function if your selection needs
     * to leave some "traces" in the context.
     *
     * @param content the content wrapped in the [sample][Sample]
     * @param context the context
     */
    protected open fun onPickSample(content: S, context: C) {}

    /**
     * **Subclasses may optionally override this.**
     *
     * This function will be called immediately before the pool is built.
     *
     * You can override this function if you need to specifically
     * tweak the settings of the pool that is being built.
     *
     * @param builder the pool builder
     */
    protected open fun onBuildPool(builder: PoolBuilder<S, C>) {}

    /**
     * Deserializes a list of filters from the node.
     */
    private fun deserializeFilters(node: ConfigurationNode): List<Filter<C>> {
        // if the node is virtual, the childrenList() will just be an empty list
        return node.childrenList().map(::filterFactory)
    }

    /**
     * Deserializes a list of samples from the node.
     */
    private fun deserializeSamples(node: ConfigurationNode): List<Sample<S, C>> {
        return node.childrenList().map { n ->
            // create sample content from the node
            val content = sampleFactory(n)

            // wrap it into a sample
            Sample.build(content) {
                weight = n.node("weight").krequire<Double>()

                // add intrinsic filters
                filters += intrinsicFilters(content)
                // add configured filters
                filters += deserializeFilters(n.node("filters"))

                // add mark if there is any
                mark = n.node("mark").string?.let { Mark.stringMarkOf(it) }

                // define trace function
                trace = {
                    // add the mark to context
                    mark?.run { it.marks += this }

                    // apply given trace function
                    onPickSample(content, it)
                }
            }
        }
    }

    final override fun deserialize(type: Type, node: ConfigurationNode): Pool<S, C> {
        when {
            // Node structure 1
            node.isList -> {
                return Pool.build {
                    samples += deserializeSamples(node)

                    // apply builder overrides
                    onBuildPool(this)
                }
            }

            // Node structure 2
            node.isMap -> {
                return Pool.build {
                    samples += deserializeSamples(node.node("entries"))
                    filters += deserializeFilters(node.node("filters"))
                    pickAmount = node.node("sample").getLong(1)
                    isReplacement = node.node("replacement").getBoolean(false)

                    // apply builder overrides
                    onBuildPool(this)
                }
            }

            else -> {
                // it's an illegal structure
                throw SerializationException("Can't serialize pool ${node.path()} due to illegal structure")
            }
        }
    }
}