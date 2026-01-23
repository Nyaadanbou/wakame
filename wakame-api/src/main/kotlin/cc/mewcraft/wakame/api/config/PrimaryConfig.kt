package cc.mewcraft.wakame.api.config

import net.kyori.adventure.key.Key

interface PrimaryConfig {

    /**
     * 可以点亮下界传送门的维度名字列表 (命名空间形式).
     */
    val netherPortalFunctionalDimensions: Set<Key>

    /**
     * 是否在设置玩家生命值缩放时打印堆栈信息.
     */
    val printStackOnSetHealthScale: Boolean

    companion object Impl : PrimaryConfig {

        private var implementation: PrimaryConfig = object : PrimaryConfig {
            override val netherPortalFunctionalDimensions: Set<Key> = emptySet()
            override val printStackOnSetHealthScale: Boolean = false
        }

        fun setImplementation(impl: PrimaryConfig) {
            implementation = impl
        }

        override val netherPortalFunctionalDimensions: Set<Key> get() = implementation.netherPortalFunctionalDimensions
        override val printStackOnSetHealthScale: Boolean get() = implementation.printStackOnSetHealthScale
    }
}