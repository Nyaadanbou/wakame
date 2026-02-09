package cc.mewcraft.wakame.gui.towny

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.integration.towny.Town
import cc.mewcraft.wakame.integration.towny.TownyLocal
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * 城镇列表菜单.
 */
class TownListMenu(
    viewer: Player,
) : PagedGovernmentListMenu(
    viewer = viewer,
) {
    companion object {
        private val uiSettings by townyHookConfig.entryOrElse(
            BasicMenuSettings(
                title = Component.text("全部城镇 (当前位面)"),
                structure = arrayOf(
                    ". . . . ? . . . .",
                    ". x x x x x x x .",
                    ". x x x x x x x .",
                    ". x x x x x x x .",
                    ". x x x x x x x .",
                    "< . . . . . . . >",
                ),
                icons = hashMapOf(
                    "background" to Identifiers.of("internal/menu/common/default/background"),
                    "prev_page" to Identifiers.of("internal/menu/common/default/prev_page"),
                    "next_page" to Identifiers.of("internal/menu/common/default/next_page"),
                    "hint" to Identifiers.of("internal/menu/towny/townlist/hint"),
                    "entry" to Identifiers.of("internal/menu/towny/townlist/entry"),
                )
            ), "town_list_menu", "ui_settings"
        )
    }

    override val uiSettings: BasicMenuSettings
        get() = Companion.uiSettings

    override fun getGovernments(): List<Town> {
        return TownyLocal.getTowns().toList()
    }
}