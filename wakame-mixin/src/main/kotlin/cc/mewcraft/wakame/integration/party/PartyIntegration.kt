package cc.mewcraft.wakame.integration.party

import cc.mewcraft.wakame.integration.party.PartyIntegration.Companion.createParty
import cc.mewcraft.wakame.integration.party.PartyIntegration.Companion.lookupPartyById
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

interface PartyIntegration {

    /**
     * Create a new [Party] with the specified name.
     *
     * Parties with no users will not be saved. Use [Party.disband] to discard.
     *
     * The returned reference will expire after one minute, store [Party.id] rather than the instance and use [lookupPartyById] to retrieve.
     *
     * @param name party name
     * @return new party
     */
    fun createParty(name: Component): Party

    /**
     * Look up an existing party by its id.
     *
     * As parties that have never had a user are not saved, they are not retrievable here.
     *
     * The returned reference will expire after one minute, do not cache it. The implementation handles caching as is appropriate.
     *
     * @param id party id
     * @return existing party
     * @see createParty
     */
    fun lookupPartyById(id: UUID): CompletableFuture<Party?>

    fun lookupPartyByPlayer(player: Player): CompletableFuture<Party?>

    /**
     * This companion object holds current [PartyIntegration] implementation.
     */
    companion object : PartyIntegration {

        private var currImpl: PartyIntegration = object : PartyIntegration {
            override fun createParty(name: Component): Party {
                return Party.noOp()
            }

            override fun lookupPartyById(id: UUID): CompletableFuture<Party?> {
                return CompletableFuture.completedFuture(null)
            }

            override fun lookupPartyByPlayer(player: Player): CompletableFuture<Party?> {
                return CompletableFuture.completedFuture(null)
            }
        }

        fun setImplementation(impl: PartyIntegration) {
            currImpl = impl
        }

        override fun createParty(name: Component): Party {
            return currImpl.createParty(name)
        }

        override fun lookupPartyById(id: UUID): CompletableFuture<Party?> {
            return currImpl.lookupPartyById(id)
        }

        override fun lookupPartyByPlayer(player: Player): CompletableFuture<Party?> {
            return currImpl.lookupPartyByPlayer(player)
        }
    }
}