package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.BiIdentifiable
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.adventure.key.Keyed
import net.kyori.examination.Examinable
import org.jetbrains.annotations.ApiStatus


/**
 * 代表一个铭刻类型.
 */
interface Kizami : Keyed, Examinable, FriendlyNamed, BiIdentifiable<String, Byte>

/**
 * 用于获取 [Kizami] 的实例.
 */
interface KizamiProvider {

    /**
     * 通过唯一标识符获取 [Kizami].
     */
    fun get(id: String): Kizami?

    /**
     * 伴生对象, 用于获取 [KizamiProvider] 的实例.
     */
    companion object Provider {
        private var instance: KizamiProvider? = null

        @JvmStatic
        fun instance(): KizamiProvider {
            return instance ?: throw IllegalStateException("KizamiProvider has not been initialized.")
        }

        @ApiStatus.Internal
        fun register(provider: KizamiProvider) {
            instance = provider
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }
    }
}