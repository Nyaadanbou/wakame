package cc.mewcraft.wakame.integration.towny

/**
 * 用来获取服务器上所有 [Town] 和 [Nation] 的接口.
 */
interface GovernmentListProvider {

    /**
     * 获取服务器上的所有 [Town].
     */
    fun getTowns(): Collection<Town>

    /**
     * 返回服务器上的所有 [Nation].
     */
    fun getNations(): Collection<Nation>

    companion object Impl : GovernmentListProvider {

        private var implementation: GovernmentListProvider = object : GovernmentListProvider {
            override fun getTowns(): Collection<Town> = emptyList()
            override fun getNations(): Collection<Nation> = emptyList()
        }

        fun setImplementation(provider: GovernmentListProvider) {
            implementation = provider
        }

        override fun getTowns(): Collection<Town> = implementation.getTowns()
        override fun getNations(): Collection<Nation> = implementation.getNations()
    }
}