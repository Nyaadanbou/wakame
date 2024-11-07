package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.recycle.WtfRecyclingStation
import cc.mewcraft.wakame.reforge.repair.RepairingTable
import cc.mewcraft.wakame.reforge.repair.WtfRepairingTable
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import java.util.stream.Stream

internal data object WtfBlacksmithStation : BlacksmithStation {
    override val recyclingStation: RecyclingStation = WtfRecyclingStation
    override val repairingTable: RepairingTable = WtfRepairingTable
}

internal class SimpleBlacksmithStation(
    override val recyclingStation: RecyclingStation,
    override val repairingTable: RepairingTable,
) : BlacksmithStation, Examinable {
    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("recyclingStation", recyclingStation),
            ExaminableProperty.of("repairingTable", repairingTable),
        )
    }

    override fun toString(): String {
        return StringExaminer.simpleEscaping().examine(this)
    }
}