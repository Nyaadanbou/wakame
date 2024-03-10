package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiEffect
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

@PreWorldDependency(runBefore = [AbilityRegistry::class, AttributeRegistry::class])
@ReloadDependency(runBefore = [AbilityRegistry::class, AttributeRegistry::class])
object KizamiRegistry : KoinComponent, Initializable,
    Registry<String, Kizami> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    /**
     * Root configuration node.
     */
    private val root: NekoConfigurationNode
        get() = get<NekoConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)).load()

    /**
     * Table used to query the kizami effect, given kizami and amount.
     */
    private val table: Table<Kizami, Int, KizamiEffect<*>> = HashBasedTable.create()

    private fun loadConfiguration() {
        @OptIn(InternalApi::class)
        clearBoth()

        root.node("kizami").childrenMap().forEach { (_, n) ->
            val kizami = n.requireKt<Kizami>()
            registerBothMapping(kizami.key, kizami)
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}