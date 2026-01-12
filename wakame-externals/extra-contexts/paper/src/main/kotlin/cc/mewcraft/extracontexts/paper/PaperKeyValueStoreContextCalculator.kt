package cc.mewcraft.extracontexts.paper

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ImmutableContextSet
import org.bukkit.entity.Player

class PaperKeyValueStoreContextCalculator(
    private val provider: KeyValueStoreContextProvider,
) : ContextCalculator<Player> {

    override fun calculate(target: Player, consumer: ContextConsumer) {
        val contexts = provider.getContexts(target.uniqueId)
        for ((key, value) in contexts) {
            consumer.accept(key, value)
        }
    }

    override fun estimatePotentialContexts(): ImmutableContextSet {
        return ImmutableContextSet.empty()
    }
}

