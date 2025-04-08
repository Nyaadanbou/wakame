package cc.mewcraft.wakame.api.element

import net.kyori.adventure.key.Keyed
import net.kyori.examination.Examinable
import org.jetbrains.annotations.ApiStatus


/**
 * 代表一个元素类型.
 */
interface Element : Keyed, Examinable

/**
 * 用于获取 [Element] 的实例.
 */
interface ElementProvider {

    /**
     * 通过 [Element.key] 获取 [Element] 实例.
     */
    fun get(id: String): Element?

    /**
     * 伴生对象, 用于获取 [ElementProvider] 的实例.
     */
    companion object {
        @get:JvmStatic
        @get:JvmName("getInstance")
        lateinit var INSTANCE: ElementProvider
            private set

        @Deprecated("Use INSTANCE instead.", ReplaceWith("INSTANCE"))
        @JvmStatic
        fun instance(): ElementProvider {
            return this.INSTANCE
        }

        @ApiStatus.Internal
        fun register(provider: ElementProvider) {
            this.INSTANCE = provider
        }
    }

}