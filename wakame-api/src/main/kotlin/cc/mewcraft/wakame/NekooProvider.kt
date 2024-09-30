package cc.mewcraft.wakame

import org.jetbrains.annotations.ApiStatus

/**
 * 提供静态函数来获取 Nekoo 实例.
 */
object NekooProvider {
    @JvmStatic
    private var nekoo: Nekoo? = null

    /**
     * 获取 Nekoo 实例.
     *
     * @throws IllegalStateException 如果 Nekoo 尚未加载
     * @return Nekoo 实例
     */
    @JvmStatic
    fun get(): Nekoo {
        return nekoo ?: throw IllegalStateException("Nekoo is not loaded yet")
    }

    @ApiStatus.Internal
    fun register(nekoo: Nekoo) {
        this.nekoo = nekoo
    }

    @ApiStatus.Internal
    fun unregister() {
        this.nekoo = null
    }
}