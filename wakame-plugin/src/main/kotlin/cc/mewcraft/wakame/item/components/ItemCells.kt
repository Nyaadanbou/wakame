package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_EXTERNALS
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
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
import cc.mewcraft.wakame.util.value
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimap
import io.leangen.geantyref.TypeToken
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

interface ItemCells : Examinable, TooltipProvider.Cluster, Iterable<Map.Entry<String, Cell>> {

    companion object : ItemComponentBridge<ItemCells>, ItemComponentMeta {
        /**
         * 构建一个 [ItemCells] 的实例.
         */
        fun of(cells: Map<String, Cell>): ItemCells {
            return Value(cells)
        }

        /**
         * 返回一个 [ItemCells] 的构建器.
         */
        fun builder(): Builder {
            return BuilderImpl()
        }

        override fun codec(id: String): ItemComponentType<ItemCells> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.CELLS
        override val tooltipKey: Key = ItemComponentConstants.createKey { CELLS }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
    }

    /**
     * 词条栏的数量.
     */
    val size: Int

    /**
     * 将本对象转换成一个 [Builder].
     */
    fun builder(): Builder

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
     * 判断是否包含指定的核心.
     */
    fun hasSimilar(core: Core): Boolean

    /**
     * 用于方便构建 [ItemCells].
     */
    interface Builder {
        fun has(id: String): Boolean
        fun get(id: String): Cell?
        fun put(id: String, cell: Cell): Cell?
        fun modify(id: String, block: (Cell) -> Cell)
        fun remove(id: String): Cell?
        fun build(): ItemCells
    }

    /* Internals */

    private class BuilderImpl : Builder {
        val map: HashMap<String, Cell> = HashMap()

        override fun has(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun get(id: String): Cell? {
            return map[id]
        }

        override fun put(id: String, cell: Cell): Cell? {
            return map.put(id, cell)
        }

        override fun modify(id: String, block: (Cell) -> Cell) {
            val cell = map[id] ?: return
            val newCell = block(cell)
            map.put(id, newCell)
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
        override val size: Int
            get() = cells.size

        override fun builder(): Builder {
            val builder = BuilderImpl()
            builder.map.putAll(cells)
            return builder
        }

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
            for ((id, cell) in this) {
                if (!ignoreCurse && cell.getCurse().isLocked(context)) {
                    continue // 诅咒还未解锁
                }
                val core = cell.getCoreAs(CoreTypes.ATTRIBUTE) ?: continue
                // 拼接物品 key 和词条栏 id 作为属性修饰符的 id
                val identity = context.key.value { "$it/$id" }
                val attributeModifiers = core.provideAttributeModifiers(identity)
                ret.putAll(attributeModifiers.entries)
            }
            return ret.build()
        }

        override fun collectConfiguredSkills(context: NekoStack, ignoreCurse: Boolean, ignoreVariant: Boolean): Multimap<Trigger, Skill> {
            val ret = ImmutableListMultimap.builder<Trigger, Skill>()
            for ((_, cell) in this) {
                if (!ignoreCurse && cell.getCurse().isLocked(context)) {
                    continue // 诅咒还未解锁
                }
                val core = cell.getCoreAs(CoreTypes.SKILL) ?: continue
                val variant = core.variant
                if (ignoreVariant || variant == TriggerVariant.any()) {
                    ret.put(core.trigger, core.skill)
                    continue
                }
                if (variant.id != context.variant) {
                    continue
                }
                ret.put(core.trigger, core.skill)
            }
            return ret.build()
        }

        override fun hasSimilar(core: Core): Boolean {
            return cells.values.any { cell -> cell.getCore().isSimilar(core) }
        }

        override fun provideTooltipLore(): Collection<LoreLine> {
            // showInTooltip 由核心各自的配置文件控制
            // if (!config.showInTooltip) {
            //     return emptyList()
            // }
            return cells.values.map { cell -> cell.provideTooltipLore() }
        }

        override fun iterator(): Iterator<Map.Entry<String, Cell>> {
            return cells.iterator()
        }

        private fun edit(consumer: (MutableMap<String, Cell>) -> Unit): ItemCells {
            // 优化: 词条栏绝大部分情况都是遍历, 而很少查询, 因此用 ArrayMap 更好
            val cells = Object2ObjectArrayMap(this.cells)
            consumer.invoke(cells)
            return Value(Object2ObjectArrayMap(cells)) // 显式副本
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemCells> {
        override fun read(holder: ItemComponentHolder): ItemCells? {
            val tag = holder.getTag() ?: return null
            // 优化: 词条栏绝大部分情况都是遍历, 而很少查询, 因此用 ArrayMap 更好
            val cells = Object2ObjectArrayMap<String, Cell>(tag.size())
            for (id in tag.keySet()) {
                val nbt = tag.getCompound(id)
                val cell = Cell.of(id, nbt)
                cells.put(id, cell)
            }
            return Value(cells)
        }

        override fun write(holder: ItemComponentHolder, value: ItemCells) {
            val tag = holder.getTagOrCreate()
            tag.clear() // 总是重新写入全部数据
            for ((id, cell) in value) {
                if (cell.getCore().isNoop) {
                    continue // 拥有 No-op 核心的词条栏不应该写入物品
                }
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
                val core = run {
                    val selected = templateCell.core.select(context)
                    val template = selected.firstOrNull() ?: TemplateCoreEmpty
                    template.generate(context)
                }

                // 生成诅咒
                val curse = run {
                    val selected = templateCell.curse.select(context)
                    val template = selected.firstOrNull() ?: TemplateCurseEmpty
                    template.generate(context)
                }

                // collect all and put it into the builder
                builder.put(id, Cell.of(id, core, curse))
            }
            return GenerationResult.of(builder.build())
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template>, KoinComponent {
        override val type: TypeToken<Template> = typeTokenOf()

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
        override fun decode(node: ConfigurationNode): Template {
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

        override fun childrenCodecs(): TypeSerializerCollection {
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