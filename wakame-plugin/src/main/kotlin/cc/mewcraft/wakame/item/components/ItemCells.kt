package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.components.cell.Cell
import cc.mewcraft.wakame.item.components.cell.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cell.cores.skill.CoreSkill
import cc.mewcraft.wakame.item.components.cell.template.TemplateCell
import cc.mewcraft.wakame.item.components.cell.template.TemplateCellSerializer
import cc.mewcraft.wakame.item.components.cell.template.TemplateCoreGroupSerializer
import cc.mewcraft.wakame.item.components.cell.template.TemplateCorePoolSerializer
import cc.mewcraft.wakame.item.components.cell.template.TemplateCoreSerializer
import cc.mewcraft.wakame.item.components.cell.template.TemplateCurseGroupSerializer
import cc.mewcraft.wakame.item.components.cell.template.TemplateCursePoolSerializer
import cc.mewcraft.wakame.item.components.cell.template.TemplateCurseSerializer
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.SKILL_EXTERNALS
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimap
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

interface ItemCells : TooltipProvider.Cluster, Examinable, Iterable<Map.Entry<String, Cell>> {

    /**
     * 检查指定的词条栏是否存在.
     */
    fun has(id: String): Boolean

    /**
     * 获取指定的词条栏.
     */
    fun get(id: String): Cell?

    /**
     * 添加一个词条栏.
     *
     * @return 修改过的 [ItemCells] 副本
     */
    fun put(id: String, cell: Cell): ItemCells

    /**
     * 修改一个词条栏.
     *
     * 传入函数 [consumer] 中的词条栏为 [id] 指定的词条栏.
     * 如果 [id] 指定的词条栏不存在, 则传入 [consumer] 的词条栏将是 `null`.
     *
     * 如果 [consumer] 返回的值为非空, 那么其值将写入返回的 [ItemCells];
     * 反之, 返回的 [ItemCells] 将是一个完全未经修改的副本.
     *
     * @return 修改过/完全未经修改的 [ItemCells] 副本
     */
    fun modify(id: String, consumer: (Cell?) -> Cell?): ItemCells

    /**
     * 移除一个词条栏.
     *
     * @return 修改过的 [ItemCells] 副本
     */
    fun remove(id: String): ItemCells

    /**
     * 获取**所有**词条栏上的 [AttributeModifier].
     */
    fun collectAttributeModifiers(context: NekoStack, ignoreCurse: Boolean = false): Multimap<Attribute, AttributeModifier>

    /**
     * 获取**所有**词条栏上的 [ConfiguredSkill].
     */
    fun collectConfiguredSkills(context: NekoStack, ignoreCurse: Boolean = false, ignoreVariant: Boolean = false): Multimap<Trigger, Skill>

    class Value(
        private val cells: Map<String, Cell>,
    ) : ItemCells {

        override fun has(id: String): Boolean {
            return cells.containsKey(id)
        }

        override fun get(id: String): Cell? {
            return cells[id]
        }

        override fun put(id: String, cell: Cell): ItemCells {
            return copyEdit { map ->
                map.put(id, cell)
            }
        }

        override fun modify(id: String, consumer: (Cell?) -> Cell?): ItemCells {
            return copyEdit { map ->
                val cell = consumer(map[id])
                if (cell != null) {
                    map.put(id, cell)
                }
            }
        }

        override fun remove(id: String): ItemCells {
            return copyEdit { map ->
                map.remove(id)
            }
        }

        override fun collectAttributeModifiers(context: NekoStack, ignoreCurse: Boolean): Multimap<Attribute, AttributeModifier> {
            val ret = ImmutableListMultimap.builder<Attribute, AttributeModifier>()
            for ((_, cell) in this) {
                if (!ignoreCurse && cell.curse.isLocked(context)) {
                    continue // 诅咒还未解锁
                }
                val core = cell.core as? CoreAttribute ?: continue
                val modifiers = core.provideAttributeModifiers(context.uuid)
                val entries = modifiers.entries
                ret.putAll(entries)
            }
            return ret.build()
        }

        override fun collectConfiguredSkills(context: NekoStack, ignoreCurse: Boolean, ignoreVariant: Boolean): Multimap<Trigger, Skill> {
            val ret = ImmutableListMultimap.builder<Trigger, Skill>()
            for ((_, cell) in this) {
                if (!ignoreCurse && cell.curse.isLocked(context)) {
                    continue // 诅咒还未解锁
                }
                val core = cell.core as? CoreSkill ?: continue
                val variant = core.variant
                if (variant == TriggerVariant.any()) {
                    continue
                }
                if (!ignoreVariant && context.variant != variant.id) {
                    continue
                }
                ret.put(core.trigger, core.instance)
            }
            return ret.build()
        }

        override fun provideTooltipLore(): Collection<LoreLine> {
            if (!showInTooltip) {
                return emptyList()
            }
            return cells.values.map { cell -> cell.provideTooltipLore() }
        }

        override fun iterator(): Iterator<Map.Entry<String, Cell>> {
            return cells.iterator()
        }

        private fun copyEdit(consumer: (MutableMap<String, Cell>) -> Unit): ItemCells {
            val cells = HashMap<String, Cell>(this.cells)
            consumer.invoke(cells)
            return Value(HashMap(cells)) // 显式副本
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.CELLS)
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemCells> {
        override fun read(holder: ItemComponentHolder): ItemCells? {
            val tag = holder.getTag() ?: return null
            val cells = HashMap<String, Cell>()
            for (id in tag.keySet()) {
                val nbt = tag.getCompound(id)
                val cell = Cell.of(id, nbt)
                cells.put(id, cell)
            }
            return Value(cells)
        }

        override fun write(holder: ItemComponentHolder, value: ItemCells) {
            holder.putTag() // 总是重新写入全部数据
            val tag = holder.getTag()!!
            for ((id, cell) in value) {
                tag.put(id, cell.asTag())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {

        }
    }

    data class Template(
        val cells: Map<String, TemplateCell>,
    ) : ItemTemplate<ItemCells> {
        override fun generate(context: GenerationContext): GenerationResult<ItemCells> {
            return GenerationResult.empty()
        }

        companion object : ItemTemplateType<Template>, KoinComponent {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            /**
             * ## Node structure
             * ```yaml
             * <node>:
             *   buckets:
             *     <词条栏 id>:
             *       core: <核心选择器>
             *       curse: <诅咒选择器>
             *     <词条栏 id>:
             *       core: <核心选择器>
             *       curse: <诅咒选择器>
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
             *     curse_pools:
             *       pool_1: <pool>
             *       pool_2: <pool>
             *       ...
             *     curse_groups:
             *       group_1: <group>
             *       group_2: <group>
             *       ...
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                // 先把节点的键 (Any) 转成 String
                val bucketMap: Map<String, ConfigurationNode> = node
                    .node("buckets")
                    .childrenMap()
                    .mapKeys { (id, _) ->
                        id.toString()
                    }

                val selectors: ConfigurationNode = node.node("selectors")
                val cells: LinkedHashMap<String, TemplateCell> = bucketMap.map { (id, node) ->
                    // 先把节点 “selectors” 注入到节点 “buckets.<id>”
                    node.hint(TemplateCellSerializer.HINT_NODE_SELECTORS, selectors)
                    // 再反序列化 “buckets.<id>”, 最后转成 Pair
                    id to node.krequire<TemplateCell>()
                }.toMap(LinkedHashMap()) // 显式构建为有序 Map

                return Template(cells)
            }

            override fun childSerializers(): TypeSerializerCollection {
                return TypeSerializerCollection.builder()

                    // 随机选择器
                    .kregister(TemplateCoreSerializer)
                    .kregister(TemplateCorePoolSerializer)
                    .kregister(TemplateCoreGroupSerializer)
                    .kregister(TemplateCurseSerializer)
                    .kregister(TemplateCursePoolSerializer)
                    .kregister(TemplateCurseGroupSerializer)

                    // 技能, 部分核心会用到
                    .registerAll(get(named(SKILL_EXTERNALS)))

                    // 实体类型, 部分诅咒会用到
                    .registerAll(get(named(ENTITY_TYPE_HOLDER_EXTERNALS)))

                    .build()
            }
        }
    }
}