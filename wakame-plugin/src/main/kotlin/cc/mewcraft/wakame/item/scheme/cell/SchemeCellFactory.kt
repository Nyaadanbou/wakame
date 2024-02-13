package cc.mewcraft.wakame.item.scheme.cell

import cc.mewcraft.wakame.random.Group
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode

object SchemeCellFactory {
    /**
     * Creates a [SchemeCell] from given configuration nodes.
     *
     * Note that some information about a [SchemeCell] in the configuration
     * file are possibly **scattered** by design, which means they are not
     * necessarily in a single node (i.e. [cellNode]). That's why this function
     * have three arguments: [cellNode], [coreNode] and [curseNode]. We need
     * all these nodes to construct a complete [SchemeCell].
     *
     * For example, given a such cell node:
     * ```yaml
     * id: b
     * core: group_b # it's just a path to a group node in the root
     * curse: group_b # same as above - it's just a path
     * keep_empty: true
     * can_reforge: true
     * can_override: false
     * ```
     *
     * As mentioned in the YAML comments, the values of node `core` and
     * `curse` are simply **paths** to other nodes in the root. The caller of
     * this function needs to pass these nodes to this function in order to
     * construct a complete [SchemeCell].
     *
     * @param cellNode the node holding the cell itself
     * @param coreNode the node holding the core for the cell or `null`, if it
     *     should be an empty core
     * @param curseNode the node holding the curse for the cell or `null`, if
     *     it should be an empty curse
     * @return a new [SchemeCell]
     */
    fun schemeOf(
        cellNode: ConfigurationNode,
        coreNode: ConfigurationNode?,
        curseNode: ConfigurationNode?,
    ): SchemeCell {
        // all are `false` by default if `null`
        val keepEmpty = cellNode.node("keep_empty").boolean
        val reforgeable = cellNode.node("can_reforge").boolean
        val overridable = cellNode.node("can_override").boolean

        val coreSelector = coreNode?.requireKt<SchemeCoreGroup>() ?: Group.empty()
        val curseSelector = curseNode?.requireKt<SchemeCurseGroup>() ?: Group.empty()

        return SchemeCellImpl(
            keepEmpty = keepEmpty,
            canReforge = reforgeable,
            canOverride = overridable,
            coreSelector = coreSelector,
            curseSelector = curseSelector
        )
    }
}