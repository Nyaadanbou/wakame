package cc.mewcraft.wakame

import cc.mewcraft.wakame.test.TestListener
import me.lucko.helper.plugin.ExtendedJavaPlugin

class WakamePlugin : ExtendedJavaPlugin() {
    override fun enable() {
        // Register listeners
        registerTerminableListener(TestListener()).bindWith(this)
    }
}