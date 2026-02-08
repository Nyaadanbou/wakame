package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.wakame.integration.towny.GovernmentListProvider
import com.palmergames.bukkit.towny.TownyAPI
import net.kyori.adventure.text.Component
import cc.mewcraft.wakame.integration.towny.Nation as KoishNation
import cc.mewcraft.wakame.integration.towny.Town as KoishTown

class TownyGovListProvider : GovernmentListProvider {

    private val townyApi: TownyAPI
        get() = TownyAPI.getInstance()

    override fun getTowns(): Collection<KoishTown> {
        return townyApi.towns.map { KoishTown(Component.text(it.name)) }
    }

    override fun getNations(): Collection<KoishNation> {
        return townyApi.nations.map { KoishNation(Component.text(it.name)) }
    }
}