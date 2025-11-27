package cc.mewcraft.wakame.hook.impl.carbonchat

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.party.Party
import cc.mewcraft.wakame.integration.party.PartyIntegration
import net.draycia.carbon.api.CarbonChatProvider
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import net.draycia.carbon.api.users.Party as InternalParty


@Hook(plugins = ["CarbonChat"])
object CarbonChatHook {

    init {
        PartyIntegration.setImplementation(CarbonChatPartyIntegration)
    }
}

private class CarbonChatParty(
    private val party: InternalParty,
) : Party {

    override val name: Component
        get() = party.name()
    override val id: UUID
        get() = party.id()
    override val members: Set<UUID>
        get() = party.members()

    override fun addMember(id: UUID) {
        return party.addMember(id)
    }

    override fun removeMember(id: UUID) {
        party.removeMember(id)
    }

    override fun disband() {
        party.disband()
    }
}

private object CarbonChatPartyIntegration : PartyIntegration {

    override fun createParty(name: Component): Party {
        val handle = CarbonChatProvider.carbonChat()
            .userManager()
            .createParty(name)
        val wrapped = CarbonChatParty(handle)
        return wrapped
    }

    override fun lookupPartyById(id: UUID): CompletableFuture<Party?> {
        return CarbonChatProvider.carbonChat()
            .userManager()
            .party(id)
            .thenApply { handle ->
                handle?.let(::CarbonChatParty)
            }
    }

    override fun lookupPartyByPlayer(id: UUID): CompletableFuture<Party?> {
        return CarbonChatProvider.carbonChat()
            .userManager()
            .user(id)
            .thenCompose { user ->
                user.party()
            }.thenApply { handle ->
                handle?.let(::CarbonChatParty)
            }
    }
}