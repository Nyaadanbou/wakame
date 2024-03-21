package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.kizami.EmptyKizamiEffect
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiEffect
import cc.mewcraft.wakame.kizami.KizamiInstance
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.Table
import com.google.common.collect.Tables
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
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

    fun getEffect(kizami: Kizami, amount: Int): KizamiEffect {
        return table.get(kizami, amount) ?: EmptyKizamiEffect
    }

    /**
     * Table used to query the kizami effect, given kizami and amount.
     */
    private val table: Table<Kizami, Int, KizamiEffect> = Tables.newCustomTable(Reference2ObjectOpenHashMap<Kizami, Map<Int, KizamiEffect>>()) { Int2ObjectOpenHashMap() }

    private fun loadConfiguration() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        val root = get<NekoConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)).load()
        root.node("kizami").childrenMap().forEach { (_, childNode) ->
            val kizamiInstance = childNode.requireKt<KizamiInstance>()
            val kizami = kizamiInstance.kizami

            // register kizami
            INSTANCES.register(kizami.uniqueId, kizami)
            // register bi lookup
            BI_LOOKUP.register(kizami.uniqueId, kizami.binaryId)
            // register kizami instance
            kizamiInstance.effect.forEach { (amount, effect) ->
                table.put(kizami, amount, effect)
            }
        }
    }
}