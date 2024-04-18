package cc.mewcraft.wakame.item.schema.cell

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.item.schema.NekoItemFactory
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.Group
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 蓝图词条栏的序列化器。
 *
 * 该序列化实现要求 [ConfigurationNode] 中已经存在 [NekoItemFactory.ITEM_ROOT_NODE_HINT]。
 *
 * Note that the `core` and `curse` data of a cell in the configuration
 * file are **scattered** by design, which means they are not necessarily
 * in a single node!
 *
 * For example, given a such cell node:
 * ```yaml
 * create_options:
 *   core: group_b # it's just a **path** to a group node in the root
 *   curse: group_b # same as above - it's just a **path** to a group node
 * modify_options:
 *   reforgeable: true
 * ```
 */
internal data object SchemaCellSerializer : SchemaSerializer<SchemaCell> {
    override fun deserialize(type: Type, node: ConfigurationNode): SchemaCell {
        // Get the root node from hints
        val root = run {
            val hint = NekoItemFactory.ITEM_ROOT_NODE_HINT
            node.hint(hint) ?: throw SerializationException(
                node, type, "Can't find hint ${hint.identifier()}. Did you forget to inject it?"
            )
        }

        // Deserialize the cell
        val cell = run {
            val id = node.key().toString()

            // deserialize groups of core & curse
            val createOptions = run {
                // A convenient function
                fun ConfigurationNode.findGroupNode(path: String): ConfigurationNode? = this.node(path)
                    // get nullable identifier
                    .string
                    // get the corresponding `(path)_groups` node
                    ?.let { groupName -> root.node("${path}_groups", groupName) }
                    // inject corresponding `(path)_pools` node
                    ?.also { groupNode -> groupNode.hint(AbstractGroupSerializer.SHARED_POOL_NODE_HINT, root.node("${path}_pools")) }

                val createOptNode = node.node("create_options")
                val coreGroup = createOptNode.findGroupNode("core")?.krequire<SchemaCoreGroup>() ?: Group.empty()
                val curseGroup = createOptNode.findGroupNode("curse")?.krequire<SchemaCurseGroup>() ?: Group.empty()
                SchemaCellImpl.ImmutableCreateOptions(coreGroup, curseGroup)
            }

            // deserialize other options of the cell
            val modifyOptions = run {
                val modifyOptNode = node.node("modify_options")
                val isReforgeable = modifyOptNode.node("reforgeable").getBoolean(false)
                SchemaCellImpl.ImmutableModifyOptions(isReforgeable)
            }

            // collect all
            SchemaCellImpl(
                createOptions = createOptions,
                modifyOptions = modifyOptions
            )
        }

        return cell
    }
}