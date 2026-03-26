package cc.mewcraft.wakame.integration.virtualstorage

import org.bukkit.entity.Player

interface VirtualStorageIntegration {

    /**
     * 为玩家打开指定 [id] 的 Vault.
     *
     * @param player 要打开 Vault 的玩家
     * @param id Vault 的编号
     */
    fun openVault(player: Player, id: Int)

    companion object : VirtualStorageIntegration {
        private var implementation: VirtualStorageIntegration = object : VirtualStorageIntegration {
            override fun openVault(player: Player, id: Int) = Unit
        }

        fun setImplementation(implementation: VirtualStorageIntegration) {
            this.implementation = implementation
        }

        override fun openVault(player: Player, id: Int) {
            implementation.openVault(player, id)
        }
    }
}