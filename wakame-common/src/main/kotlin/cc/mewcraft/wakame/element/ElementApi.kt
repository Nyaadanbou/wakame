package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.util.NumberIdentifiable
import cc.mewcraft.wakame.util.PlayerFriendlyNamed
import cc.mewcraft.wakame.util.StringIdentifiable
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.jetbrains.annotations.ApiStatus


/**
 * 代表一个元素类型.
 */
interface Element : Keyed, Examinable, PlayerFriendlyNamed, StringIdentifiable, NumberIdentifiable {
    // 形如 "koish:ice", "koish:fire"
    // 用于配置文件
    override val key: Key

    // FIXME 移除 binary_index, 持久化直接储存 id
    // 形如 "3" 的非零整数, 目前由配置文件手动指定, 之后由注册表自动分配
    // 用于压缩数据
    override val integerId: Int

    // 形如 "ice"(命名空间为 "koish" 可省略命名空间), "foo:ice"(命名空间不是 "koish")
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