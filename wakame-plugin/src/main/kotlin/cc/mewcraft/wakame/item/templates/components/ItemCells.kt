package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.templates.components.cells.CellArchetype
import cc.mewcraft.wakame.item.templates.components.cells.CellArchetypeSerializer
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetypeGroupSerializer
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetypePoolSerializer
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetypeSerializer
import cc.mewcraft.wakame.item.templates.components.cells.cores.EmptyCoreArchetype
import cc.mewcraft.wakame.skill2.SKILL_EXTERNALS
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import cc.mewcraft.wakame.item.components.ItemCells as ItemCellsData


data class ItemCells(
    val cells: Map<String, CellArchetype>,
) : ItemTemplate<ItemCellsData> {
    val minimumSlotAmount: Int = 1 // 更加合理的最小值?
    val maximumSlotAmount: Int = cells.size

    override val componentType: ItemComponentType<ItemCellsData> = ItemComponentTypes.CELLS

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemCellsData> {
        val builder = ItemCellsData.builder()
        for ((id, templateCell) in this.cells) {
            val core = run {
                val selected = templateCell.core.select(context)
                val template = selected.firstOrNull() ?: EmptyCoreArchetype
                template.generate(context)
            }

            // collect all and put it into the builder
            builder.put(id, Cell.of(id, core))
        }

        return ItemGenerationResult.of(builder.build())
    }

    companion object : ItemTemplateBridge<ItemCells> {
        override fun codec(id: String): ItemTemplateType<ItemCells> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemCells>, KoinComponent {
        override val type: TypeToken<ItemCells> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   buckets:
         *     <核孔 id>:
         *       core: <核心选择器>
         *     <核孔 id>:
         *       core: <核心选择器>
         *     ...
         *   selectors:
         *     core_pools:
         *       pool_1: <pool>
         *       pool_2: <pool>
         *       ...
         *     core_groups:
         *       group_1: <group>
         *       group_2: <group>
         *       ...
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemCells {
            // 先把节点的键 (Any) 转成 String
            val bucketMap: Map<String, ConfigurationNode> = node
                .node("buckets")
                .childrenMap()
                .mapKeys { (id, _) ->
                    id.toString()
                }

            val selectors: ConfigurationNode = node.node("selectors")
            val templates: LinkedHashMap<String, CellArchetype> = bucketMap
                .map { (id, node) ->
                    // 先把节点 “selectors” 注入到节点 “buckets.<id>”
                    node.hint(CellArchetypeSerializer.HINT_NODE_SELECTORS, selectors)
                    // 再反序列化 “buckets.<id>”, 最后转成 Pair
                    id to node.krequire<CellArchetype>()
                }.toMap(
                    // 显式构建为有序 Map
                    LinkedHashMap()
                )

            return ItemCells(templates)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()

                // 随机选择器
                .kregister(CellArchetypeSerializer)
                .kregister(CoreArchetypeSerializer)
                .kregister(CoreArchetypePoolSerializer)
                .kregister(CoreArchetypeGroupSerializer)

                // 技能, 部分核心会用到
                .registerAll(get(named(SKILL_EXTERNALS)))

                // 实体类型, 部分诅咒会用到
                .registerAll(get(named(ENTITY_TYPE_HOLDER_EXTERNALS)))

                .build()
        }
    }
}