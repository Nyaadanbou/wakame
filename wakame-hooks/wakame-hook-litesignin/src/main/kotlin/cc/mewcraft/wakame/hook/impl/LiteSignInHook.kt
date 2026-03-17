package cc.mewcraft.wakame.hook.impl

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.signin.SignInIntegration
import studio.trc.bukkit.litesignin.api.Storage
import java.util.*

@Hook(plugins = ["LiteSignIn"])
object LiteSignInHook {

    init {
        SignInIntegration.setImplementation(SignInIntegrationImpl())
    }
}

private class SignInIntegrationImpl : SignInIntegration {

    override fun giveRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = runCatching {
        val storage = Storage.getPlayer(playerId)
            ?: throw IllegalStateException("Storage not found: $playerId")
        storage.giveRetroactiveCard(amount)
    }

    override fun takeRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = runCatching {
        val storage = Storage.getPlayer(playerId)
            ?: throw IllegalStateException("Storage not found: $playerId")
        storage.takeRetroactiveCard(amount)
    }

    override fun setRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = runCatching {
        val storage = Storage.getPlayer(playerId)
            ?: throw IllegalStateException("Storage not found: $playerId")
        storage.setRetroactiveCard(amount, true)
    }
}