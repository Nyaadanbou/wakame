package cc.mewcraft.wakame.reforge.rerolling

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Group
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.stream.Stream

class SimpleRerollingSession(
    override val viewer: Player,
    input: NekoStack,
    override val selectionSessions: RerollingSession.SelectionSessionMap,
) : RerollingSession, KoinComponent {

    private val logger: Logger by inject()

    private val _input: NekoStack = input
    override val inputNekoStack: NekoStack
        get() = _input.clone()

    private var _output: NekoStack? = null
    override var outputNekoStack: NekoStack?
        get() = _output
        set(value) {
            _output = value
            logger.info("Rerolling session's output updated: $value")
        }

    private var _confirmed: Boolean = false
    override var confirmed: Boolean
        get() = _confirmed
        set(value) {
            _confirmed = value
            logger.info("Rerolling session's confirmed status updated: $value")
        }

    private var _frozen = false
    override var frozen
        get() = _frozen
        set(value) {
            if (_frozen) {
                logger.error("Trying to unfreeze a frozen session. This is a bug!")
                return
            }
            _frozen = value
            logger.info("Rerolling session's frozen status updated: $value")
        }

    override fun reforge(): RerollingSession.Result {
        TODO("Not yet implemented")
    }

    override fun clearSelections() {
        selectionSessions.forEach { (_, session) ->
            session.selected = false
        }
    }

    override fun returnInput(viewer: Player) {
        viewer.inventory.addItem(inputNekoStack.handle)
    }

    data class Result(
        override val item: NekoStack,
    ) : RerollingSession.Result {
        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("copy", item),
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    data class SelectionSession(
        override val id: String,
        override val rule: RerollingTable.CellRule,
        override val group: Group<TemplateCore, GenerationContext>,
        override val display: RerollingSession.SelectionSession.Display,
    ) : RerollingSession.SelectionSession, KoinComponent {
        private val logger: Logger by inject()

        private var _selected = false
        override var selected: Boolean
            get() = _selected
            set(value) {
                _selected = value
                logger.info("Cell session's selected status updated: $value")
            }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("id", id),
                ExaminableProperty.of("rule", rule),
                ExaminableProperty.of("group", group),
                ExaminableProperty.of("display", display),
                ExaminableProperty.of("selected", selected),
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    data class SelectionSessionMap(
        val map: HashMap<String, RerollingSession.SelectionSession> = HashMap(),
    ) : RerollingSession.SelectionSessionMap, KoinComponent {
        override val size: Int
            get() = map.size

        override fun get(id: String): RerollingSession.SelectionSession? {
            return map[id]
        }

        override fun put(id: String, session: RerollingSession.SelectionSession) {
            map[id] = session
        }

        override fun contains(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun iterator(): Iterator<Map.Entry<String, RerollingSession.SelectionSession>> {
            return map.iterator()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("size", size),
                ExaminableProperty.of("map", map),
            )
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

}
