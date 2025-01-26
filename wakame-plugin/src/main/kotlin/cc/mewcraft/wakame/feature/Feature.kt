package cc.mewcraft.wakame.feature

import net.kyori.adventure.text.logger.slf4j.ComponentLogger

// TODO feature 骨架

abstract class Feature {

    abstract val namespace: String // 仅为命名空间, 不带冒号“:”

    /**
     * The [ComponentLogger] of this addon.
     */
    lateinit var logger: ComponentLogger
        internal set

}