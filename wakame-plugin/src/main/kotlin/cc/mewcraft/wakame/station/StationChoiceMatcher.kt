package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXRegistry
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.stream.Stream

class StationChoiceCheckState(
    val choice: StationChoice,
    val sufficient: Boolean
) : Examinable {
    companion object {
        const val SUFFICIENT_ICON = "<green>✔</green>"
        const val INSUFFICIENT_ICON = "<red>✖</red>"
    }

    // 构建格式: "✔ 材料 *1"
    val description: Component
        get() =
            "<!i>${if (sufficient) SUFFICIENT_ICON else INSUFFICIENT_ICON} ".mini
                .append(choice.description)

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("choice", choice),
        ExaminableProperty.of("sufficient", sufficient)
    )

    override fun toString(): String =
        toSimpleString()
}

interface StationChoiceMatcher<T : StationChoiceMatcherContext> {
    fun createContext(player: Player): T
}

interface StationChoiceMatcherContext {
    /**
     * 使用合成站的玩家.
     */
    val player: Player
}

class StationChoiceMatcherContextMap(
    /**
     * 使用合成站的玩家.
     */
    val player: Player
) {
    private val data: MutableMap<StationChoiceMatcher<*>, Any> = Reference2ObjectArrayMap()

    operator fun contains(key: StationChoiceMatcher<*>): Boolean {
        return key in data
    }

    operator fun <T : StationChoiceMatcherContext> get(key: StationChoiceMatcher<T>): T {
        return data[key] as T
    }

    operator fun set(key: StationChoiceMatcher<*>, value: Any) {
        data[key] = value
    }
}


/* Internals */


//<editor-fold desc="StationChoiceMatcher">
internal object ItemChoiceMatcher : StationChoiceMatcher<ItemStationChoiceMatcherContext>, KoinComponent {
    val logger: Logger by inject()

    override fun createContext(player: Player): ItemStationChoiceMatcherContext {
        return ItemStationChoiceMatcherContext(player)
    }
}

internal object ExpChoiceMatcher : StationChoiceMatcher<ExpStationChoiceMatcherContext>, KoinComponent {
    val logger: Logger by inject()

    override fun createContext(player: Player): ExpStationChoiceMatcherContext {
        return ExpStationChoiceMatcherContext(player)
    }
}
//</editor-fold>


//<editor-fold desc="StationChoiceMatcherContext">
internal class ItemStationChoiceMatcherContext(
    override val player: Player
) : StationChoiceMatcherContext {
    val inventorySnapshot: Object2IntOpenHashMap<ItemX> = run {
        // 只搜索主背包(36格)的物品, 不搜索副手和盔甲
        val inventory = player.inventory
        val ret = Object2IntOpenHashMap<ItemX>(36)
        for (itemStack in inventory.storageContents) {
            if (itemStack == null) {
                continue
            }
            val itemX = ItemXRegistry.byItem(itemStack)
            val amount = itemStack.amount
            ret.mergeInt(itemX, amount) { old, given -> old + given }
        }
        ret
    }
}

internal class ExpStationChoiceMatcherContext(
    override val player: Player,
) : StationChoiceMatcherContext {
    var experienceSnapshot: Int = player.totalExperience
}
//</editor-fold>
