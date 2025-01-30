package cc.mewcraft.wakame.core

/**
 * 初始化 [ItemXFactoryRegistry] 的逻辑.
 */
object ItemXBootstrap {
    fun init() {
        ItemXFactoryRegistry.register("wakame", ItemXFactoryNeko)
        ItemXFactoryRegistry.register("minecraft", ItemXFactoryVanilla)
    }
}