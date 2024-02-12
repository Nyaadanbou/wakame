package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.component.KoinComponent

@Deprecated("No longer needed")
object AttributeRegistry : KoinComponent, Initializable, Reloadable {
    override fun onPreWorld() {
        // TODO("Not yet implemented")
    }

    override fun onReload() {
        // TODO("Not yet implemented")
    }
}