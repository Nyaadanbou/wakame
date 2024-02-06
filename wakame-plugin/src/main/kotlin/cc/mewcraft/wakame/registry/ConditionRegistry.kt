package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import org.koin.core.component.KoinComponent

@Deprecated("No longer needed")
object ConditionRegistry : KoinComponent, Initializable, Reloadable {
    fun get(name: String): BinaryCurse {
        TODO("Not yet implemented")
    }

    override fun onPreWorld() {
        // TODO("Not yet implemented")
    }

    override fun onReload() {
        // TODO("Not yet implemented")
    }
}