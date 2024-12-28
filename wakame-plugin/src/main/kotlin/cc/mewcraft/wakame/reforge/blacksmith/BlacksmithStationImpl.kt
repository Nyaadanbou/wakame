package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.recycle.WtfRecyclingStation
import cc.mewcraft.wakame.reforge.repair.RepairingTable
import cc.mewcraft.wakame.reforge.repair.WtfRepairingTable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import java.util.stream.Stream

internal data object WtfBlacksmithStation : BlacksmithStation {
    override val primarySettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("DO NOT USE"),
        structure = arrayOf(
            ". . . . . . . . .",
            ". . * * * * * * *",
            "s . * * * * * * *",
            ". . . . . . . . ."
        ),
        icons = hashMapOf(
            "background" to Key.key("internal:menu/common/default/background"),
            "select_repairing" to Key.key("internal:menu/blacksmith/default/select_repairing"),
            "select_recycling" to Key.key("internal:menu/blacksmith/default/select_recycling"),
        )
    )
    override val recyclingSettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("Recycling Station (Cheat Mode)"),
        structure = arrayOf(
            "i i i i i . .",
            "i i i i i . x"
        ),
        icons = hashMapOf(
            "background" to Key.key("internal:menu/common/default/background"),
            "recycle_when_confirmed" to Key.key("internal:menu/blacksmith/default/recycle_when_confirmed"),
            "recycle_when_empty" to Key.key("internal:menu/blacksmith/default/recycle_when_empty"),
            "recycle_when_unconfirmed" to Key.key("internal:menu/blacksmith/default/recycle_when_unconfirmed"),
        )
    )
    override val repairingSettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("Repairing Table (Cheat Mode)"),
        structure = arrayOf(
            "i i i i i . .",
            "* * * * * . ."
        ),
        icons = hashMapOf(
            "background" to Key.key("internal:menu/common/default/background"),
            "background2" to Key.key("internal:menu/blacksmith/default/background2"),
        )
    )

    override val recyclingStation: RecyclingStation = WtfRecyclingStation
    override val repairingTable: RepairingTable = WtfRepairingTable
    override val recyclingInventorySize: Int = BlacksmithStation.calculateRecyclingInventorySize(recyclingSettings)
}

internal class SimpleBlacksmithStation(
    /* settings */
    override val primarySettings: BasicMenuSettings,
    override val recyclingSettings: BasicMenuSettings,
    override val repairingSettings: BasicMenuSettings,
    /* stations */
    override val recyclingStation: RecyclingStation,
    override val repairingTable: RepairingTable,
) : BlacksmithStation, Examinable {
    override val recyclingInventorySize: Int = BlacksmithStation.calculateRecyclingInventorySize(recyclingSettings)

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            /* settings */
            ExaminableProperty.of("primarySettings", primarySettings),
            ExaminableProperty.of("recyclingSettings", recyclingSettings),
            ExaminableProperty.of("repairingSettings", repairingSettings),
            /* stations */
            ExaminableProperty.of("recyclingStation", recyclingStation),
            ExaminableProperty.of("repairingTable", repairingTable),
        )
    }

    override fun toString(): String {
        return StringExaminer.simpleEscaping().examine(this)
    }
}