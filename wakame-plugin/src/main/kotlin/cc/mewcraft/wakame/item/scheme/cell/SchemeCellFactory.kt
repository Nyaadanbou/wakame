package cc.mewcraft.wakame.item.scheme.cell

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

object SchemeCellFactory {
    /**
     * Creates a [SchemeCell] from given configuration nodes.
     *
     * Note that, by design, all the information about a [SchemeCell] in the
     * configuration file are possibly **scattered**, which means they are
     * not necessarily in a same node. That's why this function have three
     * arguments: [cellNode], [coreNode] and [curseNode]. We need all these
     * nodes to construct a complete [SchemeCell].
     *
     * For example, given a such cell node:
     * ```yaml
     * id: b
     * core: group_b # it's just a path to a group node
     * curse: group_b # same as above, it's just a path
     * keep_empty: false
     * can_reforge: true
     * can_override: false
     * ```
     *
     * As mentioned in the YAML comments, the `core`'s and `curse`'s values
     * are simply **paths** to wider nodes. The caller of this function needs
     * to pass these nodes to this function in order to construct a complete
     * [SchemeCell].
     *
     * @param cellNode the node holding the cell to be created
     * @param coreNode the node holding the core for the cell
     * @param curseNode the node holding the curse for the cell
     * @return a new [SchemeCell]
     */
    fun schemeOf(
        cellNode: ConfigurationNode,
        coreNode: ConfigurationNode,
        curseNode: ConfigurationNode,
    ): SchemeCell {
        // val id = cellNode.node("id").string

        val keepEmpty = cellNode.node("keep_empty").boolean
        val reforgeable = cellNode.node("can_reforge").boolean
        val overridable = cellNode.node("can_override").boolean

        val coreSelector = coreNode.get<SchemeCoreGroup>()
        val curseSelector = curseNode.get<SchemeCurseGroup>()

        checkNotNull(coreSelector)
        checkNotNull(curseSelector)

        return SchemeCellImpl(
            keepEmpty = keepEmpty,
            canReforge = reforgeable,
            canOverride = overridable,
            coreSelector = coreSelector,
            curseSelector = curseSelector
        )
    }
}