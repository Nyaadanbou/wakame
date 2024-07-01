package cc.mewcraft.wakame.item.components.cell.template

import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.item.components.cell.Cell
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random2.Group
import cc.mewcraft.wakame.random2.GroupSerializer
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 代表一个[词条栏][Cell]的模板.
 */
interface TemplateCell {
    /**
     * 核心的选择器.
     */
    val core: Group<TemplateCore, GenerationContext>

    /**
     * 诅咒的选择器.
     */
    val curse: Group<TemplateCurse, GenerationContext>

    companion object {
        /**
         * 构建一个 [TemplateCell].
         */
        fun of(core: Group<TemplateCore, GenerationContext>, curse: Group<TemplateCurse, GenerationContext>): TemplateCell {
            return Impl(core, curse)
        }
    }

    private data class Impl(
        override val core: Group<TemplateCore, GenerationContext>,
        override val curse: Group<TemplateCurse, GenerationContext>,
    ) : TemplateCell
}

/**
 * 词条栏模板的序列化器。
 *
 * 该序列化实现要求给定的节点中已经存在“选择器”的根节点.
 *
 * Note that the `core` and `curse` of a cell in the configuration
 * are **scattered** by design, which means they are not necessarily
 * in a common node!
 *
 * For example, given a such cell node:
 * ```yaml
 * <node>:
 *   core: group_b # it's just a **path** to a group node in the `cells` root
 *   curse: group_b # similar as above
 * ```
 */
internal object TemplateCellSerializer : TypeDeserializer<TemplateCell> {
    val HINT_NODE_SELECTORS: RepresentationHint<ConfigurationNode> = RepresentationHint.of("node_selectors", typeTokenOf())

    /**
     * ## Node structure
     * ```yaml
     * <node>:
     *   core: <group path>
     *   curse: <group path>
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
     *   curse_pools:
     *     pool_1: <pool>
     *     pool_2: <pool>
     *     ...
     *   curse_groups:
     *     group_1: <group>
     *     group_2: <group>
     *     ...
     * ```
     */
    override fun deserialize(type: Type, node: ConfigurationNode): TemplateCell {
        val selectors: ConfigurationNode = node.hint(HINT_NODE_SELECTORS) ?: throw SerializationException(
            node, type, "Can't find hint ${HINT_NODE_SELECTORS.identifier()}. Did you forget to inject it?"
        )

        val cell: TemplateCell = run {
            val id = node.key().toString()

            fun ConfigurationNode.find(path: String): ConfigurationNode? = this.node(path).string
                // get the `(path)_groups` node
                ?.let { groupName -> selectors.node("${path}_groups", groupName) }
                // inject `(path)_pools` node into the group node
                ?.also { groupNode -> groupNode.hint(GroupSerializer.HINT_NODE_SHARED_POOLS, selectors.node("${path}_pools")) }

            val core = node.find("core")?.krequire<Group<TemplateCore, GenerationContext>>() ?: Group.empty()
            val curse = node.find("curse")?.krequire<Group<TemplateCurse, GenerationContext>>() ?: Group.empty()

            TemplateCell.of(core, curse)
        }

        return cell
    }
}