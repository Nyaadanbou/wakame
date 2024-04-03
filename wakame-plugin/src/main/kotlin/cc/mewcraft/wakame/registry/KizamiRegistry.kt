package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiEffect
import cc.mewcraft.wakame.kizami.KizamiInstance
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

@PreWorldDependency(runBefore = [AbilityRegistry::class, AttributeRegistry::class])
@ReloadDependency(runBefore = [AbilityRegistry::class, AttributeRegistry::class])
object KizamiRegistry : KoinComponent, Initializable, BiKnot<String, Kizami, Byte> {
    override val INSTANCES: Registry<String, Kizami> = SimpleRegistry()
    override val BI_LOOKUP: BiRegistry<String, Byte> = SimpleBiRegistry()

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }

    private val EFFECTS: Registry<Kizami, KizamiInstance> = SimpleRegistry()

    fun getEffect(kizami: Kizami, amount: Int): KizamiEffect {
        return EFFECTS[kizami].getEffectBy(amount)
    }

    private fun loadConfiguration() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        val root = get<NekoConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)).load()
        root.node("kizami").childrenMap().forEach { (_, childNode) ->
            val kizamiInstance = childNode.krequire<KizamiInstance>()
            val kizami = kizamiInstance.kizami

            // register kizami
            INSTANCES.register(kizami.uniqueId, kizami)
            // register bi lookup
            BI_LOOKUP.register(kizami.uniqueId, kizami.binaryId)
            // register kizami instance
            EFFECTS.register(kizami, kizamiInstance)
        }
    }
}