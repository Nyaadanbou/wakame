package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.recycle.WtfRecyclingStation
import cc.mewcraft.wakame.reforge.repair.RepairingTable
import cc.mewcraft.wakame.reforge.repair.WtfRepairingTable
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import java.util.stream.Stream

internal data object WtfBlacksmithStation : BlacksmithStation {
    override val primaryMenuSettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("DO NOT USE"),
        structure = arrayOf(
            ". . . . . . . . .",
            ". . * * * * * * *",
            "s . * * * * * * *",
            ". . . . . . . . ."
        ),
        icons = hashMapOf(
            "background" to Identifiers.of("internal/menu/common/default/background"),
            "select_repairing" to Identifiers.of("internal/menu/blacksmith/default/select_repairing"),
            "select_recycling" to Identifiers.of("internal/menu/blacksmith/default/select_recycling"),
        )
    )
    override val recyclingMenuSettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("Recycling Station (Cheat Mode)"),
        structure = arrayOf(
            "i i i i i . .",
            "i i i i i . x"
        ),
        icons = hashMapOf(
            "background" to Identifiers.of("internal/menu/common/default/background"),
            "recycle_when_empty" to Identifiers.of("internal/menu/blacksmith/default/recycle_when_empty"),
            "recycle_when_error" to Identifiers.of("internal/menu/blacksmith/default/recycle_when_error"),
            "recycle_when_confirmed" to Identifiers.of("internal/menu/blacksmith/default/recycle_when_confirmed"),
            "recycle_when_unconfirmed" to Identifiers.of("internal/menu/blacksmith/default/recycle_when_unconfirmed"),
        )
    )
    override val repairingMenuSettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("Repairing Table (Cheat Mode)"),
        structure = arrayOf(
            "i i i i i . .",
            "* * * * * . ."
        ),
        icons = hashMapOf(
            "background" to Identifiers.of("internal/menu/common/default/background"),
            "background2" to Identifiers.of("internal/menu/blacksmith/default/background2"),
        )
    )

    override val recyclingStation: RecyclingStation = WtfRecyclingStation
    override val repairingTable: RepairingTable = WtfRepairingTable
    override val recyclingInventorySize: Int = BlacksmithStation.calculateRecyclingInventorySize(recyclingMenuSettings)
}

internal class SimpleBlacksmithStation(
    /* settings */
    override val primaryMenuSettings: BasicMenuSettings,
    override val recyclingMenuSettings: BasicMenuSettings,
    override val repairingMenuSettings: BasicMenuSettings,
    /* stations */
    override val recyclingStation: RecyclingStation,
    override val repairingTable: RepairingTable,
) : BlacksmithStation, Examinable {
    override val recyclingInventorySize: Int = BlacksmithStation.calculateRecyclingInventorySize(recyclingMenuSettings)

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            /* settings */
            ExaminableProperty.of("primaryMenuSettings", primaryMenuSettings),
            ExaminableProperty.of("recyclingMenuSettings", recyclingMenuSettings),
            ExaminableProperty.of("repairingMenuSettings", repairingMenuSettings),
            /* stations */
            ExaminableProperty.of("recyclingStation", recyclingStation),
            ExaminableProperty.of("repairingTable", repairingTable),
        )
    }

    override fun toString(): String {
        return StringExaminer.simpleEscaping().examine(this)
    }
}