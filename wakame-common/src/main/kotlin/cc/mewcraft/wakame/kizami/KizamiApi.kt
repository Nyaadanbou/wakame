package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.util.PlayerFriendlyNamed
import net.kyori.examination.Examinable
import org.jetbrains.annotations.ApiStatus

/**
 * 代表一个铭刻类型.
 */
// FIXME #373: 暂时设置为 internal 之后再做进一步迁移
internal interface Kizami : Keyed, Examinable, PlayerFriendlyNamed

/**
 * 用于获取 [Kizami] 的实例.
 */
// FIXME #373: 暂时设置为 internal 之后再做进一步迁移
internal interface KizamiProvider {

    /**
     * 通过唯一标识符获取 [Kizami].
     */
    fun get(id: String): Kizami?

    /**
     * 伴生对象, 用于获取 [KizamiProvider] 的实例.
     */
    companion object Provider {
        @get:JvmStatic
        @get:JvmName("getInstance")
        lateinit var INSTANCE: KizamiProvider
            private set

        @ApiStatus.Internal
        fun register(provider: KizamiProvider) {
            this.INSTANCE = provider
        }
    }
}