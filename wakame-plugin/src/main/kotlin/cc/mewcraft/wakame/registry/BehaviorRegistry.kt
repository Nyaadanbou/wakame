package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.schema.behavior.AttributeProvider
import cc.mewcraft.wakame.item.schema.behavior.Castable
import cc.mewcraft.wakame.item.schema.behavior.Damageable
import cc.mewcraft.wakame.item.schema.behavior.ItemBehaviorHolder
import cc.mewcraft.wakame.item.schema.behavior.KizamiProvider
import cc.mewcraft.wakame.item.schema.behavior.Statistical

@Deprecated("不再使用")
object BehaviorRegistry : Initializable {
    val INSTANCES: Registry<String, ItemBehaviorHolder> = SimpleRegistry()

    override fun onPreWorld() {
        // Register more behaviors here ...

        INSTANCES.register("attribute_provider", AttributeProvider)
        INSTANCES.register("castable", Castable)
        INSTANCES.register("damageable", Damageable)
        INSTANCES.register("kizami_provider", KizamiProvider)
        INSTANCES.register("statistical", Statistical)
    }
}