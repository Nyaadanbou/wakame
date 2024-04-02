package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.schema.behavior.Damageable
import cc.mewcraft.wakame.item.schema.behavior.ItemBehaviorHolder
import cc.mewcraft.wakame.item.schema.behavior.Statistical

object BehaviorRegistry : Initializable {
    val INSTANCES: Registry<String, ItemBehaviorHolder> = SimpleRegistry()

    override fun onPreWorld() {
        // Register more behaviors here ...
        
        INSTANCES += "damageable" to Damageable
        INSTANCES += "statistical" to Statistical
    }
}