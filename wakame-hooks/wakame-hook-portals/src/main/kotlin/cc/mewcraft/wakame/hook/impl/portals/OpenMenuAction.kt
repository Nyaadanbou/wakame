package cc.mewcraft.wakame.hook.impl.portals

import cc.mewcraft.wakame.integration.externalmenu.ExternalMenu
import net.thenextlvl.portals.Portal
import net.thenextlvl.portals.action.ActionType
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class OpenMenuAction : ActionType.Action<String> {

    override fun invoke(entity: Entity, portal: Portal, input: String): Boolean {
        if (entity !is Player) return false
        ExternalMenu.open(entity, input, emptyArray(), true)
        return true
    }
}