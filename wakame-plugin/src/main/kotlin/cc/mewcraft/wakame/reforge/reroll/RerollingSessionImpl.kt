package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Group
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.reroll.SimpleRerollingSession.Total
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.stream.Stream
import kotlin.properties.Delegates

class SimpleRerollingSession(
    override val table: RerollingTable,
    override val viewer: Player,
    inputItem: NekoStack,
    override val selections: RerollingSession.SelectionMap,
) : RerollingSession, KoinComponent {

    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.REROLL
    }

    private val logger: Logger by inject()

    override var result: RerollingSession.Result by Delegates.observable(Result.empty()) { _, _, new ->
        logger.info("$PREFIX Result status updated: $new")
    }

    override val inputItem: NekoStack by NekoStackDelegates.copyOnRead(inputItem)

    override var confirmed: Boolean by Delegates.observable(false) { _, old, new ->
        logger.info("$PREFIX Confirmation status updated: $old -> $new")
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("$PREFIX Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }

        logger.info("$PREFIX Frozen status updated: $old -> $new")
        return@vetoable true
    }

    private fun reforge0(): RerollingSession.Result {
        val operation = ReforgeOperation(this, logger)
        val result = try {
            operation.execute()
        } catch (e: Exception) {
            logger.error("$PREFIX An unknown error occurred while rerolling an item", e)
            Result.error()
        }
        return result
    }

    override fun reforge(): RerollingSession.Result {
        val ret = reforge0()
        this.result = ret
        return ret
    }

    override fun returnInput(viewer: Player) {
        viewer.inventory.addItem(inputItem.itemStack)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("viewer", viewer.name),
        ExaminableProperty.of("table", table),
    )

    override fun toString(): String =
        toSimpleString()

    object Result {
        fun empty(): RerollingSession.Result {
            return EMPTY
        }

        fun error(): RerollingSession.Result {
            return ERROR
        }

        fun success(
            cost: RerollingSession.Total,
            item: NekoStack,
        ): RerollingSession.Result {
            return Simple(true, listOf(Component.text("重造成功!")), cost, item)
        }

        private val EMPTY: RerollingSession.Result = Simple(false, listOf(Component.text("没有要重造的物品")), Total.zero(), NekoStack.empty())
        private val ERROR: RerollingSession.Result = Simple(false, listOf(Component.text("重造发生内部错误!")), Total.error(), NekoStack.empty())

        private class Simple(
            successful: Boolean,
            description: List<Component>,
            cost: RerollingSession.Total,
            item: NekoStack,
        ) : RerollingSession.Result {
            override val successful: Boolean = successful
            override val description: List<Component> = description
            override val cost: RerollingSession.Total = cost
            override val item: NekoStack by NekoStackDelegates.copyOnRead(item)

            override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
                ExaminableProperty.of("successful", successful),
                ExaminableProperty.of("description", description),
                ExaminableProperty.of("cost", cost),
                ExaminableProperty.of("item", item),
            )

            override fun toString(): String =
                toSimpleString()
        }
    }

    object Total {
        fun zero(): RerollingSession.Total {
            return ZERO
        }

        fun error(): RerollingSession.Total {
            return ERROR
        }

        fun success(default: Double): RerollingSession.Total {
            return Simple(default)
        }

        private val ZERO: RerollingSession.Total = Simple(.0)
        private val ERROR: RerollingSession.Total = Simple(Double.MAX_VALUE)

        private class Simple(
            override val default: Double,
        ) : RerollingSession.Total {
            override fun get(currency: String): Double {
                return .0 // TODO 等到需要支持多货币的时候再实现
            }

            override fun test(viewer: Player): Boolean {
                return true // TODO 接入经济账户插件
            }

            override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
                ExaminableProperty.of("default", default),
            )

            override fun toString(): String =
                toSimpleString()
        }
    }

    class Selection(
        override val id: String,
        override val rule: RerollingTable.CellRule,
        override val template: Group<TemplateCore, GenerationContext>,
        override val display: RerollingSession.SelectionDisplay,
    ) : RerollingSession.Selection, KoinComponent {
        private val logger: Logger by inject()

        override var selected: Boolean by Delegates.observable(false) { _, old, new ->
            logger.info("$PREFIX Selection status updated (cell: '$id'): $old -> $new")
        }

        override fun invert() {
            selected = !selected
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("rule", rule),
            ExaminableProperty.of("group", template),
            ExaminableProperty.of("display", display),
            ExaminableProperty.of("selected", selected),
        )

        override fun toString(): String =
            toSimpleString()
    }

    class SelectionDisplay(
        override val name: Component,
        override val lore: List<Component>,
    ) : RerollingSession.SelectionDisplay {
        override fun apply(item: ItemStack) {
            item.editMeta { meta ->
                meta.itemName(name)
                meta.lore(lore)
            }
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("name", name),
            ExaminableProperty.of("lore", lore),
        )

        override fun toString(): String =
            toSimpleString()
    }

    class SelectionMap(
        private val map: HashMap<String, RerollingSession.Selection> = HashMap(),
    ) : RerollingSession.SelectionMap, KoinComponent {
        override val size: Int
            get() = map.size

        override fun get(id: String): RerollingSession.Selection? {
            return map[id]
        }

        override fun set(id: String, session: RerollingSession.Selection) {
            map[id] = session
        }

        override fun contains(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun count(predicate: (RerollingSession.Selection) -> Boolean): Int {
            return map.count { (_, sel) -> predicate(sel) }
        }

        override fun iterator(): Iterator<Map.Entry<String, RerollingSession.Selection>> {
            return map.iterator()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("size", size),
            ExaminableProperty.of("map", map),
        )

        override fun toString(): String =
            toSimpleString()
    }
}