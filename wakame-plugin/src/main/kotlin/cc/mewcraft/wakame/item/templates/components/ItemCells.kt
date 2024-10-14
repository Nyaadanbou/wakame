package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.components.cells.*
import cc.mewcraft.wakame.item.templates.components.cells.cores.EmptyCoreBlueprint
import cc.mewcraft.wakame.skill.SKILL_EXTERNALS
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import cc.mewcraft.wakame.item.components.ItemCells as ItemCellsData


data class ItemCells(
    val cells: Map<String, CellBlueprint>,
) : ItemTemplate<ItemCellsData> {
    val minimumSlotAmount: Int = 1 // 更加合理的最小值?
    val maximumSlotAmount: Int = cells.size

    override val componentType: ItemComponentType<ItemCellsData> = ItemComponentTypes.CELLS

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemCellsData> {
        val builder = ItemCellsData.builder()
        for ((id, templateCell) in this.cells) {
            val core = run {
                val selected = templateCell.core.select(context)
                val template = selected.firstOrNull() ?: EmptyCoreBlueprint
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
         *     <词条栏 id>:
         *       core: <核心选择器>
         *     <词条栏 id>:
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
            val templates: LinkedHashMap<String, CellBlueprint> = bucketMap
                .map { (id, node) ->
                    // 先把节点 “selectors” 注入到节点 “buckets.<id>”
                    node.hint(CellBlueprintSerializer.HINT_NODE_SELECTORS, selectors)
                    // 再反序列化 “buckets.<id>”, 最后转成 Pair
                    id to node.krequire<CellBlueprint>()
                }.toMap(
                    // 显式构建为有序 Map
                    LinkedHashMap()
                )

            return ItemCells(templates)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()

                // 随机选择器
                .kregister(CellBlueprintSerializer)
                .kregister(CoreBlueprintSerializer)
                .kregister(CoreBlueprintPoolSerializer)
                .kregister(CoreBlueprintGroupSerializer)

                // 技能, 部分核心会用到
                .registerAll(get(named(SKILL_EXTERNALS)))

                // 实体类型, 部分诅咒会用到
                .registerAll(get(named(ENTITY_TYPE_HOLDER_EXTERNALS)))

                .build()
        }
    }
}