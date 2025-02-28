package cc.mewcraft.wakame.item.templates.components.cells

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.components.cells.CellArchetypeSerializer.HINT_NODE_SELECTORS
import cc.mewcraft.wakame.random3.Group
import cc.mewcraft.wakame.random3.GroupSerializer
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 代表一个 [核孔][cc.mewcraft.wakame.item.components.cells.Cell] 的模板.
 */
interface CellArchetype {
    /**
     * 核心的选择器.
     */
    val core: Group<CoreArchetype, ItemGenerationContext>
}

/**
 * [CellArchetype] 的标准实现.
 */
internal class SimpleCellArchetype(
    override val core: Group<CoreArchetype, ItemGenerationContext>
) : CellArchetype

/**
 * 核孔模板的序列化器。
 *
 * 该序列化实现要求给定的节点中已经存在“选择器”的根节点.
 *
 * 请注意，配置中的核孔的“核心”是 *分散* 的, 也就是它们不在同一个节点中!
 *
 * 例如, 给定这样一个核孔节点:
 * ```yaml
 * <node>:
 *   core: group_b # 这只是一个到指定节点的 *路径*
 * ```
 */
internal object CellArchetypeSerializer : TypeSerializer<CellArchetype> {
    val HINT_NODE_SELECTORS: RepresentationHint<ConfigurationNode> = RepresentationHint.of("node_selectors", typeTokenOf())

    /**
     * ## Node structure
     * ```yaml
     * <node>:
     *   core: <group path>
     * ```
     *
     * ## Node structure of the injected ([HINT_NODE_SELECTORS])
     * ```yaml
     * <node>:
     *   core_pools:
     *     pool_1: <pool>
     *     pool_2: <pool>
     *     ...
     *   core_groups:
     *     group_1: <group>
     *     group_2: <group>
     *     ...
     * ```
     */
    override fun deserialize(type: Type, node: ConfigurationNode): CellArchetype {
        val selectors: ConfigurationNode = node.hint(HINT_NODE_SELECTORS) ?: throw SerializationException(
            node, type, "Can't find hint ${HINT_NODE_SELECTORS.identifier()}. Did you forget to inject it?"
        )

        fun ConfigurationNode.find(path: String): ConfigurationNode? {
            return this.node(path).string
                // get the `(path)_groups` node
                ?.let { groupName -> selectors.node("${path}_groups", groupName) }
                // inject `(path)_pools` node into the group node
                ?.also { groupNode -> groupNode.hint(GroupSerializer.HINT_NODE_SHARED_POOLS, selectors.node("${path}_pools")) }
        }

        val core = node.find("core")?.require<Group<CoreArchetype, ItemGenerationContext>>() ?: Group.empty()
        val cell = SimpleCellArchetype(core)

        return cell
    }
}