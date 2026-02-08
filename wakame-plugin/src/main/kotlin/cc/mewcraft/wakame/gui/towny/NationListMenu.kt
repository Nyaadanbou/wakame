package cc.mewcraft.wakame.gui.towny

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.integration.towny.GovernmentListProvider
import cc.mewcraft.wakame.integration.towny.Nation
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * 国家列表菜单.
 */
class NationListMenu(
    viewer: Player,
) : PagedGovernmentListMenu(
    viewer = viewer,
) {

    companion object {

        private val townyConfig = ConfigAccess["hook/towny/config"]
        private val menuSettings by townyConfig.entryOrElse(
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
                    "hint" to Identifiers.of("internal/menu/towny/nationlist/hint"),
                    "entry" to Identifiers.of("internal/menu/towny/nationlist/entry"),
                )
            ), "nation_list_menu"
        )
    }

    override val settings: BasicMenuSettings
        get() = menuSettings

    override fun getGovernments(): List<Nation> {
        return GovernmentListProvider.getNations().toList()
    }
}