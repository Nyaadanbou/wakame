package cc.mewcraft.wakame.item.schema.cell

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.item.schema.NekoItemFactory
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import cc.mewcraft.wakame.random.AbstractGroupSerializer
import cc.mewcraft.wakame.random.Group
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 空的蓝图词条栏。
 */
internal data object EmptySchemaCell : SchemaCell {
    override val id: String = "empty"
    override val isReforgeable: Boolean = false
    override val isOverridable: Boolean = false
    override val keepEmpty: Boolean = false
    override val core: Group<SchemaCore, SchemaGenerationContext> = Group.empty()
    override val curse: Group<SchemaCurse, SchemaGenerationContext> = Group.empty()
}

/**
 * 不可变的蓝图词条栏。
 */
internal data class ImmutableSchemaCell(
    override val id: String,
    override val keepEmpty: Boolean,
    override val isReforgeable: Boolean,
    override val isOverridable: Boolean,
    override val core: SchemaCoreGroup,
    override val curse: SchemaCurseGroup,
) : SchemaCell

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
 * id: b
 * core: group_b # it's just a **path** to a group node in the root
 * curse: group_b # same as above - it's just a **path** to a group node
 * keep_empty: true
 * can_reforge: true
 * can_override: false
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

        // A convenient function
        fun findNode(path: String, sourceNode: ConfigurationNode): ConfigurationNode? = sourceNode.node(path)
            // get nullable identifier
            .string
            // get the corresponding `(path)_groups` node
            ?.let { groupName -> root.node("${path}_groups", groupName) }
            // inject corresponding `(path)_pools` node
            ?.also { groupNode -> groupNode.hint(AbstractGroupSerializer.SHARED_POOL_NODE_HINT, root.node("${path}_pools")) }

        // Deserialize the cell
        val cell = run {
            val id = node.node("id").krequire<String>()

            // deserialize groups of core & curse
            val coreGroup = findNode("core", node)?.krequire<SchemaCoreGroup>() ?: Group.empty()
            val curseGroup = findNode("curse", node)?.krequire<SchemaCurseGroup>() ?: Group.empty()

            // deserialize other options of the cell
            val keepEmpty = node.node("keep_empty").getBoolean(false)
            val isReforgeable = node.node("can_reforge").getBoolean(false)
            val isOverridable = node.node("can_override").getBoolean(false)

            // collect all
            ImmutableSchemaCell(
                id = id,
                keepEmpty = keepEmpty,
                isReforgeable = isReforgeable,
                isOverridable = isOverridable,
                core = coreGroup,
                curse = curseGroup
            )
        }

        return cell
    }
}
