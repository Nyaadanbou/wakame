package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.scheme.behavior.Damageable
import cc.mewcraft.wakame.item.scheme.behavior.ItemBehaviorHolder
import cc.mewcraft.wakame.item.scheme.behavior.Statistical

object BehaviorRegistry : Initializable {
    val INSTANCES: Registry<String, ItemBehaviorHolder> = SimpleRegistry()

    override fun onPreWorld() {
        INSTANCES += "damageable" to Damageable
        INSTANCES += "statistical" to Statistical
    }
}