package cc.mewcraft.wakame.skill2.external

import cc.mewcraft.wakame.skill2.external.component.Cooldown

object ExternalSupport {
    val FACTORIES: Array<ExternalComponentFactory<*, *>> = arrayOf(
        Cooldown
    )
}