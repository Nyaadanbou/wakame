package cc.mewcraft.extracontexts.velocity

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import com.velocitypowered.api.proxy.Player
import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ImmutableContextSet

class VelocityKeyValueStoreContextCalculator(
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

