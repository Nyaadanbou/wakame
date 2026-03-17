package cc.mewcraft.wakame.integration.signin

import java.util.*

interface SignInIntegration {

    companion object : SignInIntegration {
        private var implementation: SignInIntegration = object : SignInIntegration {
            override fun giveRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = Result.failure(UnsupportedOperationException())
            override fun takeRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = Result.failure(UnsupportedOperationException())
            override fun setRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = Result.failure(UnsupportedOperationException())
        }

        fun setImplementation(implementation: SignInIntegration) {
            this.implementation = implementation
        }

        override fun giveRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = implementation.giveRetroactiveCard(playerId, amount)
        override fun takeRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = implementation.takeRetroactiveCard(playerId, amount)
        override fun setRetroactiveCard(playerId: UUID, amount: Int): Result<Unit> = implementation.setRetroactiveCard(playerId, amount)
    }

    fun giveRetroactiveCard(playerId: UUID, amount: Int): Result<Unit>

    fun takeRetroactiveCard(playerId: UUID, amount: Int): Result<Unit>

    fun setRetroactiveCard(playerId: UUID, amount: Int): Result<Unit>
}