package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.trigger.TriggerVariant
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.isVirtual
import cc.mewcraft.wakame.util.withValue
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import net.kyori.examination.Examinable


interface ItemCells : Examinable, Iterable<Map.Entry<String, Cell>> {

    companion object : ItemComponentBridge<ItemCells> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.CELLS)

        /**
         * 构建一个 [ItemCells] 的实例.
         */
        fun of(): ItemCells {
            return Value(emptyMap())
        }

        /**
         * 构建一个 [ItemCells] 的实例, 初始值为给定的参数
         */
        fun of(cells: Map<String, Cell>): ItemCells {
            return Value(cells)
        }

        /**
         * 返回一个 [ItemCells] 的建造者.
         */
        fun builder(): Builder {
            return BuilderImpl()
        }

        override fun codec(id: String): ItemComponentType<ItemCells> {
            return Codec(id)
        }
    }

    /**
     * 核孔的数量.
     */
    val size: Int

    /**
     * 将本对象转换成一个 [Builder].
     */
    fun builder(): Builder

    /**
     * 过滤出符合条件的核孔.
     */
    // 命名为 filter2 是为了避免跟 Iterable#filter 相冲突
    fun filter2(predicate: (Cell) -> Boolean): ItemCells

    /**
     * 检查指定的核孔是否存在.
     */
    fun has(id: String): Boolean

    /**
     * 获取指定的核孔.
     */
    fun get(id: String): Cell?

    /**
     * 添加一个核孔.
     *
     * @return 修改过的副本
     */
    fun put(id: String, cell: Cell): ItemCells

    /**
     * 修改一个核孔.
     *
     * 传入函数 [block] 中的核孔为 [id] 所指定的核孔.
     * 函数 [block] 所返回的核孔将写入 [id] 指定的位置.
     *
     * 如果 [id] 指定的核孔不存在, 则 [block] 不会执行;
     * 这种情况下, 该函数所返回的 [ItemCells] 是未经修改的副本.
     *
     * @return 修改过/完全未经修改的副本
     */
    fun modify(id: String, block: (Cell) -> Cell): ItemCells

    /**
     * 移除一个核孔.
     *
     * @return 修改过的副本
     */
    fun remove(id: String): ItemCells

    /**
     * 获取所有核孔上的 [AttributeModifier].
     */
    fun collectAttributeModifiers(context: NekoStack, slot: ItemSlot): Multimap<Attribute, AttributeModifier>

    /**
     * 获取所有核孔上的 [PlayerAbility].
     */
    fun collectAbilityModifiers(context: NekoStack, slot: ItemSlot): Collection<PlayerAbility>

    /**
     * 忽略数值的前提下, 判断是否包含指定的核心.
     */
    fun containSimilarCore(core: Core): Boolean

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

        override fun filter2(predicate: (Cell) -> Boolean): ItemCells {
            return edit { map ->
                map.entries.removeIf { (_, cell) -> !predicate(cell) }
            }
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

        override fun collectAttributeModifiers(context: NekoStack, slot: ItemSlot): Multimap<Attribute, AttributeModifier> {
            val ret = ImmutableListMultimap.builder<Attribute, AttributeModifier>()
            for ((id, cell) in this) {
                val core = cell.getCore() as? AttributeCore ?: continue
                val attribute = core.attribute
                val sourceId = context.id.withValue { "$it/${slot.slotIndex}/$id" }
                val attributeModifiers = attribute.provideAttributeModifiers(sourceId)
                ret.putAll(attributeModifiers.entries)
            }
            return ret.build()
        }

        override fun collectAbilityModifiers(context: NekoStack, slot: ItemSlot): Collection<PlayerAbility> {
            val ret = ArrayList<PlayerAbility>()
            for ((_, cell) in this) {
                val core = cell.getCore() as? AbilityCore ?: continue
                val ability = core.ability

                val abilityVariant = ability.variant
                if (abilityVariant == TriggerVariant.any()) {
                    ret.add(ability)
                    continue
                }
                if (abilityVariant.id != context.variant) {
                    continue
                }
                ret.add(ability)
            }
            return ret
        }

        override fun containSimilarCore(core: Core): Boolean {
            return cells.values.any { cell -> cell.getCore().similarTo(core) }
        }

        override fun iterator(): Iterator<Map.Entry<String, Cell>> {
            return cells.iterator()
        }

        private fun edit(consumer: (MutableMap<String, Cell>) -> Unit): ItemCells {
            // 优化: 核孔绝大部分情况都是遍历, 而很少查询, 因此用 ArrayMap 更好
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

            // 优化: 核孔绝大部分情况都是遍历, 而很少查询, 因此用 ArrayMap 更好
            val cells = Object2ObjectArrayMap<String, Cell>(tag.size())

            for (id in tag.keySet()) {
                val nbt = tag.getCompound(id)
                val cell = Cell.of(id, nbt)
                cells.put(id, cell)
            }

            return Value(cells)
        }

        override fun write(holder: ItemComponentHolder, value: ItemCells) {
            holder.editTag { tag ->
                tag.clear() // 总是重新写入全部数据

                for ((id, cell) in value) {
                    if (cell.getCore().isVirtual) {
                        continue // 拥有虚拟核心的核孔不应该写入物品
                    }

                    tag.put(id, cell.serializeAsTag())
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }
}