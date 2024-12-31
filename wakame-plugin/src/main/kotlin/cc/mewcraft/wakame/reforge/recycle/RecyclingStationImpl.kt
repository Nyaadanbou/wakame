package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import java.util.stream.Stream
import kotlin.random.Random

internal data object WtfRecyclingStation : RecyclingStation {
    override val id: String = "wtf"
    override val random: Random = Random(123)

    private val ZERO_PRICE_INSTANCE = PriceInstance(.0, emptyMap())

    override fun getPrice(key: Key): PriceInstance {
        return ZERO_PRICE_INSTANCE
    }
}

internal class SimpleRecyclingStation(
    override val id: String,
    private val items: Set<Key>,
) : RecyclingStation {
    override val random: Random = Random(0)

    override fun getPrice(key: Key): PriceInstance? {
        if (key !in items) {
            return null
        }

        return RecyclingStationRegistry.getItem(key)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("items", items),
        )
    }

    override fun toString(): String {
        return StringExaminer.simpleEscaping().examine(this)
    }
}