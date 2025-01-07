package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.core.NumberRepresentable
import cc.mewcraft.wakame.core.PlayerFriendlyNamed
import cc.mewcraft.wakame.core.StringRepresentable
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.jetbrains.annotations.ApiStatus


/**
 * 代表一个元素类型.
 */
interface Element : Keyed, Examinable, PlayerFriendlyNamed, StringRepresentable, NumberRepresentable {
    // 形如 "koish:ice", "koish:fire"
    // 用于配置文件
    override val key: Key

    // 形如 "3", "5", 目前由配置文件手动指定
    // 用于压缩数据
    override val integerId: Int

    // 形如 "ice", "koish:ice"(带命名空间)
    // 用于持久化
    override val stringId: String
}

/**
 * 用于获取 [Element] 的实例.
 */
interface ElementProvider {

    /**
     * 通过 [Element.stringId] 获取 [Element] 实例.
     */
    fun get(stringId: String): Element?

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