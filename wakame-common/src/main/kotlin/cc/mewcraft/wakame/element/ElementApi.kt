package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.BiIdentifiable
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.adventure.key.Keyed
import net.kyori.examination.Examinable
import org.jetbrains.annotations.ApiStatus


/**
 * 代表一个元素类型.
 */
interface Element : Keyed, Examinable, FriendlyNamed, BiIdentifiable<String, Byte>

/**
 * 用于获取 [Element] 的实例.
 */
interface ElementProvider {

    /**
     * 通过唯一标识符获取 [Element].
     */
    fun get(id: String): Element?

    /**
     * 伴生对象, 用于获取 [ElementProvider] 的实例.
     */
    companion object Provider {
        private var instance: ElementProvider? = null

        @JvmStatic
        fun instance(): ElementProvider {
            return instance ?: throw IllegalStateException("ElementProvider has not been initialized.")
        }

        @ApiStatus.Internal
        fun register(provider: ElementProvider) {
            instance = provider
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }
    }
}