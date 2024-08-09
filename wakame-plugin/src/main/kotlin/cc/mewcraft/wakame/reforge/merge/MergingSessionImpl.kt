package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.stream.Stream
import kotlin.properties.Delegates

class SimpleMergingSession(
    override val viewer: Player,
    override val table: MergingTable,
) : MergingSession, KoinComponent {
    private val logger: Logger by inject()

    constructor(
        viewer: Player,
        table: MergingTable,
        inputItem1: NekoStack?,
        inputItem2: NekoStack?,
    ) : this(viewer, table) {
        this.inputItem1 = inputItem1
        this.inputItem2 = inputItem2
    }

    override var inputItem1: NekoStack? by NekoStackDelegates.nullableCopyOnWrite(null)
    override var inputItem2: NekoStack? by NekoStackDelegates.nullableCopyOnWrite(null)

    override fun returnInputItem1(viewer: Player) {
        inputItem1?.let { viewer.inventory.addItem(it.unsafe.handle) }
    }

    override fun returnInputItem2(viewer: Player) {
        inputItem2?.let { viewer.inventory.addItem(it.unsafe.handle) }
    }

    override var result: MergingSession.Result by Delegates.vetoable(Result.failure()) { _, old, new ->
        if (frozen) {
            logger.error("Trying to set result of a frozen merging session. This is a bug!")
            return@vetoable false
        }
        logger.info("Updating MergingSession result")
        return@vetoable true
    }

    override fun merge(): MergingSession.Result {
        TODO("Not yet implemented")
    }

    override val frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Unfreezing a frozen merging session. This is a bug!")
            return@vetoable false
        }
        logger.info("Freezing MergingSession frozen")
        return@vetoable true
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("viewer", viewer),
        ExaminableProperty.of("table", table),
    )

    override fun toString(): String =
        toSimpleString()

    class Result
    private constructor(
        override val successful: Boolean,
        override val item: NekoStack,
        override val cost: MergingSession.Cost,
    ) : MergingSession.Result {
        companion object {
            fun success(item: NekoStack, cost: MergingSession.Cost): Result {
                return Result(true, item, cost)
            }

            fun failure(): Result {
                return Result(false, NekoStack.empty(), Cost.zero())
            }
        }
    }

    object Cost {
        fun zero(): MergingSession.Cost {
            return Zero
        }

        fun error(): MergingSession.Cost {
            return Error
        }

        fun normal(): MergingSession.Cost {
            return Normal()
        }

        private object Zero : MergingSession.Cost {
            override fun take(viewer: Player) = Unit
            override fun test(viewer: Player): Boolean = true
        }

        private object Error : MergingSession.Cost {
            override fun take(viewer: Player): Unit = throw IllegalStateException("This cost is not supposed to be taken.")
            override fun test(viewer: Player): Boolean = throw IllegalStateException("This cost is not supposed to be tested.")
        }

        private class Normal : MergingSession.Cost {
            override fun take(viewer: Player) {
                // TODO 实现 take, 还有下面的 test
            }

            override fun test(viewer: Player): Boolean {
                return true
            }
        }
    }
}