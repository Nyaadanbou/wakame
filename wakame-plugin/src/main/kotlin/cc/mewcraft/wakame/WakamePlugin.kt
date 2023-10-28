package cc.mewcraft.wakame

// import cc.mewcraft.wakame.nms.ItemStackNms
// import cc.mewcraft.wakame.nms.ItemStackNmsImp
import cc.mewcraft.wakame.test.TestListener
import me.lucko.helper.plugin.ExtendedJavaPlugin

class WakamePlugin : ExtendedJavaPlugin() {
    override fun enable() {
        // Initialize NMS
        // ItemStackNms.provider = ItemStackNmsImp()

        // Register listeners
        registerTerminableListener(TestListener()).bindWith(this)
    }
}