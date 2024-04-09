package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.schema.behavior.*

object BehaviorRegistry : Initializable {
    val INSTANCES: Registry<String, ItemBehaviorHolder> = SimpleRegistry()

    override fun onPreWorld() {
        // Register more behaviors here ...

        INSTANCES += "attribute_provider" to AttributeProvider
        INSTANCES += "castable" to Castable
        INSTANCES += "damageable" to Damageable
        INSTANCES += "kizami_provider" to KizamiProvider
        INSTANCES += "statistical" to Statistical
    }
}