package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Group
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
    private val logger: Logger by inject()

    override var result: RerollingSession.Result by Delegates.observable(Result.empty()) { _, _, new ->
        logger.info("Rerolling session's result updated: $new")
    }

    override val inputItem: NekoStack by NekoStackDelegates.readOnly(inputItem)

    override var confirmed: Boolean by Delegates.observable(false) { _, _, new ->
        logger.info("Rerolling session's confirmed status updated: $new")
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }
        logger.info("Rerolling session's frozen status updated: $new")
        return@vetoable true
    }

    override fun select(id: String) {
        selections[id]?.selected = true
    }

    override fun unselected(id: String) {
        selections[id]?.selected = false
    }

    override fun reforge(): RerollingSession.Result {
        val operation = ReforgeOperation(this, logger)
        try {
            result = operation.execute()
        } catch (e: ReforgeOperationException) {
            logger.warn("An error occurred while reforging an item: ${e.message}")
            result = Result.error()
        } catch (e: Exception) {
            logger.error("An unknown error occurred while reforging an item", e)
            result = Result.error()
        }
        return result
    }

    override fun returnInput(viewer: Player) {
        viewer.inventory.addItem(inputItem.unsafe.handle)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("viewer", viewer.name),
            ExaminableProperty.of("table", table),
        )
    }

    class Result(
        override val successful: Boolean,
        override val cost: RerollingSession.Result.TotalCost,
        item: NekoStack,
    ) : RerollingSession.Result {

        override val item: NekoStack by NekoStackDelegates.readOnly(item)

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("successful", successful),
            ExaminableProperty.of("cost", cost),
            ExaminableProperty.of("item", item),
        )

        override fun toString(): String = toSimpleString()

        companion object {
            private val EMPTY: Result = Result(false, TotalCost.zero(), NekoStack.empty())
            private val ERROR: Result = Result(false, TotalCost.error(), NekoStack.empty())

            fun success(
                cost: RerollingSession.Result.TotalCost,
                item: NekoStack,
            ): Result {
                return Result(true, cost, item)
            }

            fun empty(): Result {
                return EMPTY
            }

            fun error(): Result {
                return ERROR
            }
        }

        data class TotalCost(
            override val default: Double,
        ) : RerollingSession.Result.TotalCost {
            companion object {
                private val ZERO: TotalCost = TotalCost(.0)
                private val ERROR: TotalCost = TotalCost(Double.MAX_VALUE)

                fun zero(): TotalCost {
                    return ZERO
                }

                fun error(): TotalCost {
                    return ERROR
                }
            }

            override fun get(currency: String): Double {
                return .0 // TODO 等到需要支持多货币的时候再实现
            }

            override fun test(viewer: Player): Boolean {
                return true // TODO 接入经济账户插件
            }

            override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
                ExaminableProperty.of("default", default),
            )

            override fun toString(): String = toSimpleString()
        }
    }

    class Selection(
        override val id: String,
        override val rule: RerollingTable.CellRule,
        override val template: Group<TemplateCore, GenerationContext>,
        override val display: RerollingSession.Selection.Display,
    ) : RerollingSession.Selection, KoinComponent {
        private val logger: Logger by inject()

        override var selected: Boolean by Delegates.observable(false) { _, _, new ->
            logger.info("Cell session's selected status updated: $new")
        }

        override fun invertSelect() {
            selected = !selected
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("rule", rule),
            ExaminableProperty.of("group", template),
            ExaminableProperty.of("display", display),
            ExaminableProperty.of("selected", selected),
        )

        override fun toString(): String = toSimpleString()

        class Display(
            override val name: Component,
            override val lore: List<Component>,
        ) : RerollingSession.Selection.Display {
            override fun apply(item: ItemStack) {
                item.editMeta { meta ->
                    meta.itemName(name)
                    meta.lore(lore)
                }
            }
        }
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

        override fun toString(): String = toSimpleString()
    }
}