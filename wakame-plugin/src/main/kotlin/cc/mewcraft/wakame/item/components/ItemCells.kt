package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponent
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.CoreTypes
import cc.mewcraft.wakame.item.components.cells.template.TemplateCell
import cc.mewcraft.wakame.item.components.cells.template.TemplateCellSerializer
import cc.mewcraft.wakame.item.components.cells.template.TemplateCoreGroupSerializer
import cc.mewcraft.wakame.item.components.cells.template.TemplateCorePoolSerializer
import cc.mewcraft.wakame.item.components.cells.template.TemplateCoreSerializer
import cc.mewcraft.wakame.item.components.cells.template.TemplateCurseGroupSerializer
import cc.mewcraft.wakame.item.components.cells.template.TemplateCursePoolSerializer
import cc.mewcraft.wakame.item.components.cells.template.TemplateCurseSerializer
import cc.mewcraft.wakame.item.components.cells.template.cores.empty.TemplateCoreEmpty
import cc.mewcraft.wakame.item.components.cells.template.curses.TemplateCurseEmpty
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

interface ItemCells : Examinable, ItemComponent, TooltipProvider.Cluster, Iterable<Map.Entry<String, Cell>> {

    companion object : ItemComponentBridge<ItemCells> {
        fun of(cells: Map<String, Cell>): ItemCells {
            return Value(cells)
        }

        fun builder(): Builder {
            return BuilderImpl()
        }

        override fun codec(id: String): ItemComponentType<ItemCells> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Template> {
            return TemplateType
        }
    }

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
     * @return 修改过的副本
     */
    fun put(id: String, cell: Cell): ItemCells

    /**
     * 修改一个词条栏.
     *
     * 传入函数 [block] 中的词条栏为 [id] 所指定的词条栏.
     * 函数 [block] 所返回的词条栏将写入 [id] 指定的位置.
     *
     * 如果 [id] 指定的词条栏不存在, 则 [block] 不会执行;
     * 这种情况下, 该函数所返回的 [ItemCells] 是未经修改的副本.
     *
     * @return 修改过/完全未经修改的副本
     */
    fun modify(id: String, block: (Cell) -> Cell): ItemCells

    /**
     * 移除一个词条栏.
     *
     * @return 修改过的副本
     */
    fun remove(id: String): ItemCells

    /**
     * 获取所有词条栏上的 [AttributeModifier].
     */
    fun collectAttributeModifiers(context: NekoStack, ignoreCurse: Boolean = false): Multimap<Attribute, AttributeModifier>

    /**
     * 获取所有词条栏上的 [ConfiguredSkill].
     */
    fun collectConfiguredSkills(context: NekoStack, ignoreCurse: Boolean = false, ignoreVariant: Boolean = false): Multimap<Trigger, Skill>

    /**
     * 用于方便构建 [ItemCells].
     */
    interface Builder {
        fun has(id: String): Boolean
        fun get(id: String): Cell?
        fun put(id: String, cell: Cell): Cell?
        fun remove(id: String): Cell?
        fun build(): ItemCells
    }

    /* Internals */

    private class BuilderImpl : Builder {
        private val map: HashMap<String, Cell> = HashMap()

        override fun has(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun get(id: String): Cell? {
            return map[id]
        }

        override fun put(id: String, cell: Cell): Cell? {
            return map.put(id, cell)
        }

        override fun remove(id: String): Cell? {
            return map.remove(id)
        }

        override fun build(): ItemCells {
            return Value(map)
        }
    }

    private data class Value(
        private val cells: Map<String, Cell>,
    ) : ItemCells {

        override fun has(id: String): Boolean {
            return cells.containsKey(id)
        }

        override fun get(id: String): Cell? {
            return cells[id]
        }

        override fun put(id: String, cell: Cell): ItemCells {
            return edit { map ->
                map.put(id, cell)
            }
        }

        override fun modify(id: String, block: (Cell) -> Cell): ItemCells {
            return edit { map ->
                val cell = map[id]
                if (cell != null) {
                    val newCell = block(cell)
                    map.put(id, newCell)
                }
            }
        }

        override fun remove(id: String): ItemCells {
            return edit { map ->
                map.remove(id)
            }
        }

        override fun collectAttributeModifiers(context: NekoStack, ignoreCurse: Boolean): Multimap<Attribute, AttributeModifier> {
            val ret = ImmutableListMultimap.builder<Attribute, AttributeModifier>()
            for ((_, cell) in this) {
                if (!ignoreCurse && cell.getCurse().isLocked(context)) {
                    continue // 诅咒还未解锁
                }
                val core = cell.getTypedCore(CoreTypes.ATTRIBUTE) ?: continue
                val modifiers = core.provideAttributeModifiers(context.uuid)
                val entries = modifiers.entries
                ret.putAll(entries)
            }
            return ret.build()
        }

        override fun collectConfiguredSkills(context: NekoStack, ignoreCurse: Boolean, ignoreVariant: Boolean): Multimap<Trigger, Skill> {
            val ret = ImmutableListMultimap.builder<Trigger, Skill>()
            for ((_, cell) in this) {
                if (!ignoreCurse && cell.getCurse().isLocked(context)) {
                    continue // 诅咒还未解锁
                }
                val core = cell.getTypedCore(CoreTypes.SKILL) ?: continue
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

        private fun edit(consumer: (MutableMap<String, Cell>) -> Unit): ItemCells {
            val cells = HashMap<String, Cell>(this.cells)
            consumer.invoke(cells)
            return Value(HashMap(cells)) // 显式副本
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.CELLS)
    }

    private data class Codec(
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
                tag.put(id, cell.serializeAsTag())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object
    }

    data class Template(
        val cells: Map<String, TemplateCell>,
    ) : ItemTemplate<ItemCells> {
        override val componentType: ItemComponentType<ItemCells> = ItemComponentTypes.CELLS

        override fun generate(context: GenerationContext): GenerationResult<ItemCells> {
            val builder = builder()
            for ((id, templateCell) in this.cells) {
                // 生成核心
                val generatedCore = run {
                    val templateCore = templateCell.core.pickSingle(context) ?: TemplateCoreEmpty
                    templateCore.generate(context)
                }

                // 生成诅咒
                val generatedCurse = run {
                    val templateCurse = templateCell.curse.pickSingle(context) ?: TemplateCurseEmpty
                    templateCurse.generate(context)
                }

                // collect all and put it into the builder
                builder.put(id, Cell.of(id, generatedCore, generatedCurse))
            }
            return GenerationResult.of(builder.build())
        }
    }

    private data object TemplateType : ItemTemplateType<Template>, KoinComponent {
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
            val templates: LinkedHashMap<String, TemplateCell> = bucketMap
                .map { (id, node) ->
                    // 先把节点 “selectors” 注入到节点 “buckets.<id>”
                    node.hint(TemplateCellSerializer.HINT_NODE_SELECTORS, selectors)
                    // 再反序列化 “buckets.<id>”, 最后转成 Pair
                    id to node.krequire<TemplateCell>()
                }.toMap(
                    // 显式构建为有序 Map
                    LinkedHashMap()
                )

            return Template(templates)
        }

        override fun childSerializers(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()

                // 随机选择器
                .kregister(TemplateCellSerializer)
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