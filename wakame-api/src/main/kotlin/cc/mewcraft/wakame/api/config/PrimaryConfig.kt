package cc.mewcraft.wakame.api.config

import net.kyori.adventure.key.Key

interface PrimaryConfig {

    /**
     * 可以点亮下界传送门的维度名字列表.
     */
    val netherPortalFunctionalDimensions: Set<Key>

    companion object Impl : PrimaryConfig {

        private var implementation: PrimaryConfig = object : PrimaryConfig {
            override val netherPortalFunctionalDimensions: Set<Key> = emptySet()
        }

        fun setImplementation(impl: PrimaryConfig) {
            implementation = impl
        }

        override val netherPortalFunctionalDimensions: Set<Key> get() = implementation.netherPortalFunctionalDimensions
    }
}