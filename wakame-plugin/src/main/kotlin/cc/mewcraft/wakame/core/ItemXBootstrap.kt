package cc.mewcraft.wakame.core

/**
 * 初始化 [ItemXRegistry] 的逻辑.
 */
object ItemXBootstrap {
    fun init() {
        ItemXRegistry.register("wakame", ItemXFactoryNeko)
        ItemXRegistry.register("minecraft", ItemXFactoryVanilla)
    }
}