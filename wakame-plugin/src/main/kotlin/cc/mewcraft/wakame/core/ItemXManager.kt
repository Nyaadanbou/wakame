package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.ReloadableProperty

object ItemXManager {
    private val INSTANCES: HashMap<String, ItemX> by ReloadableProperty { HashMap(32) }

}